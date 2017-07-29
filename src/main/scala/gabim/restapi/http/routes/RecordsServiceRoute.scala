package gabim.restapi.http.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.PathMatchers.IntNumber
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.PathMatchers.IntNumber
import de.heikoseeberger.akkahttpcirce.CirceSupport
import gabim.restapi.http.SecurityDirectives
import gabim.restapi.models.{RecordEntityUpdate, UserEntityUpdate}
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

  implicit val TimestampFormat : Encoder[DateTime] with Decoder[DateTime] = new Encoder[DateTime] with Decoder[DateTime] {
    override def apply(a: DateTime): Json = Encoder.encodeLong.apply(a.getMillis)

    override def apply(c: HCursor): Result[DateTime] = Decoder.decodeLong.map(s => new DateTime(s)).apply(c)
  }

  val route = pathPrefix("records") {
      pathPrefix(IntNumber) { id =>
        authenticate { loggedUser =>
          (pathEndOrSingleSlash & authorize(usersService canViewRecords  loggedUser)) {
            get {
              complete(getRecordsByUserId(id).map(_.asJson))
            } ~
              post {
                entity(as[RecordEntityUpdate]) { recordUpdate =>
                  complete(updateRecord(id, recordUpdate).map(_.asJson))
                }
              } ~
              delete {
                onSuccess(deleteRecord(id)) { ignored =>
                  complete(NoContent)
                }
              }
          }
        }
      }
  }

}
