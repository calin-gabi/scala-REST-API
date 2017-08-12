package gabim.restapi.web

import akka.http.scaladsl.model.{HttpEntity, MediaTypes, StatusCodes}
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import akka.http.scaladsl.server
import gabim.restapi.models.{TokenEntity, UserEntity, UserResponseEntity}
import io.circe.Decoder.Result
import io.circe.{Decoder, Encoder, HCursor, Json}
import io.circe.generic.auto._
import io.circe.syntax._
import org.joda.time.DateTime

class AuthServiceTest extends BaseServiceTest{

  trait Context {
    val passwords = randomPasswords(2)
    val testUsers = provisionUsersList(passwords, "user")
    val route = httpService.authRouter.route
  }

  implicit val TimestampFormat : Encoder[DateTime] with Decoder[DateTime] = new Encoder[DateTime] with Decoder[DateTime] {
    override def apply(a: DateTime): Json = Encoder.encodeLong.apply(a.getMillis)

    override def apply(c: HCursor): Result[DateTime] = Decoder.decodeLong.map(s => new DateTime(s)).apply(c)
  }


  private def signUpUser(user: UserEntity, route: server.Route)(action: => Unit) = {
    val requestEntity = HttpEntity(MediaTypes.`application/json`, user.asJson.noSpaces)
    Post("/auth/signUp", requestEntity) ~> route ~> check(action)
  }

  private def signInUser(user: UserEntity, clearpass: String, route: server.Route)(action: => Unit) = {
    val requestEntity = HttpEntity(
      MediaTypes.`application/json`,
      s"""{"username": "${user.username}", "password": "${clearpass}"}"""
    )
    Post("/auth/signIn", requestEntity) ~> route ~> check(action)
  }

  "Authentication service" should {

    "register user and return token" in new Context {
      val testUser = testUsers(0)
      signUpUser(testUser, route) {
        response.status should be(StatusCodes.Created)
      }
    }

    "authenticate user and return user response {username, token, profile}" in new Context {
      val testUser = testUsers(1)
      val pass = passwords(1)
      signInUser(testUser, pass, route) {
        println(response)
        responseAs[UserResponseEntity] should be
      }
    }
  }
}
