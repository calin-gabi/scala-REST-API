package gabim.restapi.http.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._

import com.github.t3hnar.bcrypt.BCrypt
import org.mindrot.jbcrypt.BCrypt

import gabim.restapi.services.AuthService
import gabim.restapi.models.{UserEntity, UserResponseEntity}

import de.heikoseeberger.akkahttpcirce.CirceSupport
import io.circe.Decoder.Result
import io.circe.{Decoder, Encoder, HCursor, Json}
import io.circe.generic.auto._
import io.circe.syntax._

import org.joda.time.DateTime

import scala.concurrent.ExecutionContext


class AuthServiceRoute(val authService: AuthService)
                      (implicit executionContext: ExecutionContext)
  extends CirceSupport {

  import StatusCodes._
  import authService._
  import databaseService._
  import databaseService.driver.api._

  implicit val TimestampFormat : Encoder[DateTime] with Decoder[DateTime] = new Encoder[DateTime] with Decoder[DateTime] {
    override def apply(a: DateTime): Json = Encoder.encodeLong.apply(a.getMillis)

    override def apply(c: HCursor): Result[DateTime] = Decoder.decodeLong.map(s => new DateTime(s)).apply(c)
  }

  val route =
    pathPrefix("auth") {
    path("signIn") {
      pathEndOrSingleSlash {
        post {
          entity(as[LoginPassword]){ loginPassword =>
            complete(signIn(loginPassword.username, loginPassword.password).map(_.asJson))
          }
        }
      }
    } ~
      path("isAuthenticated") {
        pathEndOrSingleSlash {
          (get & optionalHeaderValueByName("Token")) { token =>
            complete(
              authService.authenticate(token.getOrElse("")).map(_.asJson)
            )
          }
        }
      } ~
      path("signUp") {
        pathEndOrSingleSlash {
          post {
            entity(as[UserEntity]) { userEntity =>
              complete(Created -> signUp(userEntity).map(_.asJson))
            }
          }
        }
      }~
      path("signOut") {
        pathEndOrSingleSlash {
          (post & optionalHeaderValueByName("Token")) { token =>
            complete(authService.deleteToken(token.get))
          }
        }
      }
  }

  private case class LoginPassword(username: String, password: String)

  private case class Token(token: String)

}
