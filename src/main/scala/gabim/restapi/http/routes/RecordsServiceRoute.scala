package gabim.restapi.http.routes

import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.PathMatchers.IntNumber
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.PathMatchers.IntNumber
import de.heikoseeberger.akkahttpcirce.CirceSupport
import gabim.restapi.http.SecurityDirectives
import gabim.restapi.models.{RecordEntity, RecordEntityUpdate, UserEntityUpdate}
import gabim.restapi.services.{AuthService, RecordsService, UsersService}
import io.circe.Decoder.Result
import io.circe.{Decoder, Encoder, HCursor, Json}
import io.circe.generic.auto._
import io.circe.syntax._
import akka.event.Logging
import org.joda.time.DateTime

import scala.concurrent.ExecutionContext

class RecordsServiceRoute(val authService: AuthService,
                          recordsService: RecordsService,
                          usersService: UsersService
                         )(implicit executionContext: ExecutionContext) extends CirceSupport with SecurityDirectives {

  import StatusCodes._
  import recordsService._


  implicit def recordsRejectionHandler =
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
      .handleNotFound {
        complete((NotFound, "Not here!")) }
      .result()

  implicit val TimestampFormat : Encoder[DateTime] with Decoder[DateTime] = new Encoder[DateTime] with Decoder[DateTime] {
    override def apply(a: DateTime): Json = Encoder.encodeLong.apply(a.getMillis)

    override def apply(c: HCursor): Result[DateTime] = Decoder.decodeLong.map(s => new DateTime(s)).apply(c)
  }

  val route = pathPrefix("records") {
    handleRejections(recordsRejectionHandler) {
      pathPrefix(LongNumber) { userId => {
        authenticate { loggedUser =>
          (pathEndOrSingleSlash & authorize(recordsService canViewRecords(loggedUser, userId)))  {
            get{
              complete(getRecordsByUserId(userId).map(_.asJson))
            } ~
              (post & authorize(recordsService canUpdateRecords(loggedUser, userId))){
                entity(as[RecordEntity]) { newRecord =>
                  complete(createRecord(newRecord))
                }
              }
            } ~
            pathPrefix(LongNumber) { recordId => {
              (pathEndOrSingleSlash & authorize(recordsService canUpdateRecords(loggedUser, userId))){
                post {
                  entity(as[RecordEntityUpdate]) { recordUpdate => {
                    complete(Created -> updateRecord(recordId, recordUpdate).map(_.asJson))
                  }
                  }
                } ~
                  delete {
                    onSuccess(deleteRecord(recordId)) { ignored =>
                      complete(Accepted -> s"record ${recordId} deleted")
                    }
                  }
                }
              }
            } ~
            pathPrefix("filter") {
              post {
                entity(as[FilterRecordsEntity]) { filterRecordsEntity =>
                  complete(filterRecord(userId, filterRecordsEntity.startDate, filterRecordsEntity.endDate).map(_.asJson))
                }
              }
            }
          }
        }
      }
    }
  }

  private case class FilterRecordsEntity(startDate: DateTime, endDate: DateTime)
}
