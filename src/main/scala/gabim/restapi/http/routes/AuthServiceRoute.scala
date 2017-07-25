package gabim.restapi.http.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.{BasicHttpCredentials, HttpCredentials}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.Credentials
import akka.parboiled2.util.Base64
import akka.http.scaladsl.model.HttpCharsets._
import com.github.t3hnar.bcrypt.BCrypt
import org.mindrot.jbcrypt.BCrypt
import de.heikoseeberger.akkahttpcirce.CirceSupport
import gabim.restapi.services.AuthService
import gabim.restapi.services.UsersService

import scala.concurrent.{ExecutionContext, Future}
import gabim.restapi.http.SecurityDirectives
import gabim.restapi.models.UserEntity
import io.circe.Decoder.Result
import io.circe.{Decoder, Encoder, HCursor, Json}
import io.circe.generic.auto._
import io.circe.syntax._
import org.joda.time.DateTime


class AuthServiceRoute(val authService: AuthService)(implicit executionContext: ExecutionContext) extends CirceSupport with SecurityDirectives {

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
          (post & extractCredentials) { credentials => {
            val token = credentials.get.token()
            val bytes = Base64.rfc2045.decodeFast(token)
            val userPass = (new String(bytes, `UTF-8`.nioCharset)).split(":")
            val loginPassword = new LoginPassword(userPass(0), userPass(1))
            complete(signIn(loginPassword.login, loginPassword.password).map(_.asJson))
          }
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
      }
  }

  private case class LoginPassword(login: String, password: String)

}
