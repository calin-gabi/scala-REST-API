package gabim.restapi.http.routes

import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.PathMatchers.IntNumber
import akka.http.scaladsl.server._
import de.heikoseeberger.akkahttpcirce.CirceSupport
import gabim.restapi.services.{AuthService, UsersService}

import scala.concurrent.ExecutionContext
import gabim.restapi.http.SecurityDirectives
import gabim.restapi.models.{UserEntity, UserEntityUpdate, UsernameAvailable}
import org.joda.time.DateTime
import com.github.tototoshi.slick.PostgresJodaSupport._
import io.circe.Decoder.Result
import io.circe.{Decoder, Encoder, HCursor, Json}
import io.circe.generic.auto._
import io.circe.syntax._
import akka.event.Logging

class UsersServiceRoute(val authService: AuthService,
                        usersService: UsersService
                       )(implicit executionContext: ExecutionContext) extends CirceSupport with SecurityDirectives {

  import StatusCodes._
  import usersService._

  implicit val TimestampFormat : Encoder[DateTime] with Decoder[DateTime] = new Encoder[DateTime] with Decoder[DateTime] {
    override def apply(a: DateTime): Json = Encoder.encodeLong.apply(a.getMillis)

    override def apply(c: HCursor): Result[DateTime] = Decoder.decodeLong.map(s => new DateTime(s)).apply(c)
  }

  implicit def usersRejectionHandler =
    RejectionHandler.newBuilder()
      .handle { case MissingHeaderRejection("Token") =>
        complete(HttpResponse(BadRequest, entity = "No token, no service!!!"))
      }
      .handle { case AuthorizationFailedRejection =>
        complete((Forbidden, "You have no power here!"))
      }
      .handle { case ValidationRejection(msg, _) =>
        complete((InternalServerError, "That wasn't valid! " + msg))
      }
      .handleAll[MethodRejection] { methodRejections =>
      val names = methodRejections.map(_.supported.name)
      complete((MethodNotAllowed, s"Can't do that! Supported: ${names mkString " or "}!"))
    }
      .handleNotFound { complete((NotFound, "Not here!")) }
      .result()

  val route = pathPrefix("users") {
    handleRejections(usersRejectionHandler) {
      pathEndOrSingleSlash {
        authenticate { loggedUser =>
          get {
            authorize(usersService canViewUsers loggedUser) {
              complete(getUsers().map(_.asJson))
            }
          }
        }
      } ~
        pathPrefix("isAvailable") {
          pathEndOrSingleSlash {
            post {
              entity(as[UsernameAvailable]) { username =>
                complete(isAvailable(username.username).map(_.asJson))
              }
            }
          }
        } ~
        pathPrefix("me") {
          pathEndOrSingleSlash {
            authenticate { loggedUser =>
              get {
                complete(loggedUser)
              } ~
                post {
                  entity(as[UserEntityUpdate]) { userUpdate =>
                    complete(updateUser(loggedUser.id.get, userUpdate).map(_.asJson))
                  }
                }
            }
          }
        } ~
        pathPrefix(LongNumber) { id =>
          authenticate { loggedUser =>
            (pathEndOrSingleSlash & authorize(usersService canUpdateUsers loggedUser)) {
              get {
                complete(getUserById(id).map(_.asJson))
              } ~
                post {
                  entity(as[UserEntityUpdate]) { userUpdate =>
                    complete(updateUser(id, userUpdate).map(_.asJson))
                  }
                } ~
                delete {
                  onSuccess(deleteUser(id)) { ignored =>
                    complete(NoContent)
                  }
                }
            }
          }
        }
    }
  }

}
