package gabim.restapi.http.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import de.heikoseeberger.akkahttpcirce.CirceSupport
import gabim.restapi.models.OAuthToken
import gabim.restapi.services.OAuthService
import io.circe.Decoder.Result
import io.circe.{Decoder, Encoder, HCursor, Json}
import io.circe.generic.auto._
import io.circe.syntax._
import org.joda.time.DateTime

import scala.concurrent.ExecutionContext

class OAuthServiceRoute(val oauthService: OAuthService) (implicit executionContext: ExecutionContext) extends CirceSupport {

  import StatusCodes._
  import oauthService._
  import databaseService._
  import databaseService.driver.api._

  implicit val TimestampFormat : Encoder[DateTime] with Decoder[DateTime] = new Encoder[DateTime] with Decoder[DateTime] {
    override def apply(a: DateTime): Json = Encoder.encodeLong.apply(a.getMillis)

    override def apply(c: HCursor): Result[DateTime] = Decoder.decodeLong.map(s => new DateTime(s)).apply(c)
  }

  val route =
    pathPrefix("oauth") {
//      path("register") {
//        pathEndOrSingleSlash {
//          post {
//            entity(as[OAuthToken]) { oauthToken =>
//              complete(Created -> signUpOAuth(oauthToken).map(_.asJson))
//            }
//          }
//        }
//      } ~
        path("login") {
          pathEndOrSingleSlash {
            post {
              entity(as[OAuthToken]) { oauthToken => oauthToken.oauthType match {
                  case "google" => complete(Created -> loginOAuth(oauthToken).map(_.asJson))
                  case whoa => complete(NoContent)
                }
              }
            }
          }
        } // ~
//      path("verify") {
//        pathEndOrSingleSlash {
//          post {
//            entity(as[OAuthToken]) { oauthToken =>
//              verifyIdToken(oauthToken.token)
//              complete(NoContent)
//            }
//          }
//        }
//      } ~
//        path("tokeninfo") {
//          pathEndOrSingleSlash {
//            post {
//              entity(as[OAuthToken]) { oauthToken =>
//                tokenInfo(oauthToken.token)
//                complete(NoContent)
//              }
//            }
//          }
//        }
    }

}
