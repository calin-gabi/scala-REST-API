package gabim.restapi.web

import akka.http.javadsl.model.StatusCodes
import akka.http.scaladsl.model.StatusCodes.NoContent
import akka.http.scaladsl.model.{HttpEntity, HttpHeader, MediaTypes}
import akka.http.scaladsl.model.headers.{Authorization, BasicHttpCredentials}
import akka.http.scaladsl.server.AuthorizationFailedRejection
import gabim.restapi.models.UserEntity
import io.circe.Decoder.Result
import io.circe.{Decoder, Encoder, HCursor, Json}
import org.joda.time.DateTime
import org.scalatest.concurrent.ScalaFutures

class UsersServiceTest extends BaseServiceTest with ScalaFutures{

  import usersService._

  implicit val TimestampFormat : Encoder[DateTime] with Decoder[DateTime] = new Encoder[DateTime] with Decoder[DateTime] {
    override def apply(a: DateTime): Json = Encoder.encodeLong.apply(a.getMillis)

    override def apply(c: HCursor): Result[DateTime] = Decoder.decodeLong.map(s => new DateTime(s)).apply(c)
  }

  trait userContext {
    val passwords = randomPasswords(1)
    val testUsers = provisionUsersList(passwords, "user")
    val testTokens = provisionTokensForUsers(testUsers)
    val route = httpService.usersRouter.route
  }

  trait adminContext {
    val passwords = randomPasswords(1)
    val testUsers = provisionUsersList(passwords, "admin")
    val testTokens = provisionTokensForUsers(testUsers)
    val route = httpService.usersRouter.route
  }

  trait managerContext {
    val passwords = randomPasswords(1)
    val testUsers = provisionUsersList(passwords, "manager")
    val testTokens = provisionTokensForUsers(testUsers)
    val route = httpService.usersRouter.route
  }
//
//  "The users service " should {
//
//    "on get users: return 400 Bad Request if not authenticated" in new userContext {
//      Get("/users") ~> route ~> check {
//        response.status should be(StatusCodes.BAD_REQUEST)
//      }
//    }
//
//    "on get users: return 403 Forbidden if role is user" in new userContext {
//      val testUser = testUsers(0)
//      val authorization = "Token" -> testTokens.find(_.userId.contains(testUser.id.get)).get.token
//      Get("/users") ~> addHeader(authorization._1, authorization._2) ~> route ~> check {
//        response.status should be(StatusCodes.FORBIDDEN)
//      }
//    }
//
//    "on get users: return list if role is manager" in new adminContext {
//      val testUser = testUsers(0)
//      val authorization = "Token" -> testTokens.find(_.userId.contains(testUser.id.get)).get.token
//      Get("/users") ~> addHeader(authorization._1, authorization._2) ~> route ~> check {
//        response.status should be(StatusCodes.OK)
//      }
//    }
//
//    "on get users: return list if role is admin" in new adminContext {
//      val testUser = testUsers(0)
//      val authorization = "Token" -> testTokens.find(_.userId.contains(testUser.id.get)).get.token
//      Get("/users") ~> addHeader(authorization._1, authorization._2) ~> route ~> check {
//        response.status should be(StatusCodes.OK)
//      }
//    }
//
//    "on get me: return logged user" in new userContext {
//      val testUser = testUsers(0)
//      val authorization = "Token" -> testTokens.find(_.userId.contains(testUser.id.get)).get.token
//      Get("/users/me") ~> addHeader(authorization._1, authorization._2) ~> route ~> check {
//        response.status should be(StatusCodes.OK)
//      }
//    }
//
//    "get user by id" in new adminContext {
//      val testUser = testUsers(0)
//      val userId = testUser.id.get
//      val authorization = "Token" -> testTokens.find(_.userId.contains(userId)).get.token
//      Get(s"/users/${userId}") ~> addHeader(authorization._1, authorization._2) ~> route ~> check {
//        response.status should be(StatusCodes.OK)
//        //responseAs[UserEntity] should be(testUser)
//      }
//    }
//
//    "delete user by id" in new adminContext {
//      val testUser = testUsers(0)
//      val userId = testUser.id.get
//      val authorization = "Token" -> testTokens.find(_.userId.contains(userId)).get.token
//      Delete(s"/users/${userId}") ~> addHeader(authorization._1, authorization._2) ~> route ~> check {
//        response.status should be(NoContent)
//        whenReady(getUserById(testUser.id.get)) { result =>
//          result should be(None: Option[UserEntity])
//        }
//      }
//    }
//
//    "on post me: update logged user" in new userContext {
//      val testUser = testUsers(0)
//      val newEmail = RandomString(10)
//      val requestEntity = HttpEntity(MediaTypes.`application/json`, s"""{"email": "$newEmail"}""")
//      val authorization = "Token" -> testTokens.find(_.userId.contains(testUser.id.get)).get.token
//      Post("/users/me", requestEntity) ~> addHeader(authorization._1, authorization._2) ~> route ~> check {
//        response.status should be(StatusCodes.OK)
//        whenReady(getUserById(testUser.id.get)) { result =>
//          //println(result)
//          result.get.email should be(Option(newEmail))
//        }
//      }
//    }
//  }

}
