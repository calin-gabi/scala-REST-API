package gabim.restapi.web

import akka.http.javadsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import io.circe.Decoder.Result
import io.circe.{Decoder, Encoder, HCursor, Json}
import org.joda.time.DateTime
import org.scalatest.concurrent.ScalaFutures

class RecordsServiceTest extends BaseServiceTest with ScalaFutures{
  import usersService._

  implicit val TimestampFormat : Encoder[DateTime] with Decoder[DateTime] = new Encoder[DateTime] with Decoder[DateTime] {
    override def apply(a: DateTime): Json = Encoder.encodeLong.apply(a.getMillis)

    override def apply(c: HCursor): Result[DateTime] = Decoder.decodeLong.map(s => new DateTime(s)).apply(c)
  }

  trait userContext {
    val passwords = randomPasswords(1)
    val testUsers = provisionUsersList(passwords, "user")
    val testTokens = provisionTokensForUsers(testUsers)
    val testRecords = provisionRecordsList(testUsers)
    val route = httpService.recordsRouter.route
  }

  trait adminContext {
    val passwords = randomPasswords(1)
    val testUsers = provisionUsersList(passwords, "admin")
    val testTokens = provisionTokensForUsers(testUsers)
    val route = httpService.recordsRouter.route
  }

  trait managerContext {
    val passwords = randomPasswords(1)
    val testUsers = provisionUsersList(passwords, "manager")
    val testTokens = provisionTokensForUsers(testUsers)
    val route = httpService.recordsRouter.route
  }

  "The records service " should {

    "on get: return records of selected user" in new managerContext {
      val testUser = testUsers(0)
      val userId = testUser.id.get
      val authorization = "Token" -> testTokens.find(_.userId.contains(userId)).get.token
      val url = s"/records/${userId}"
      Get(url) ~> addHeader(authorization._1, authorization._2) ~> route ~> check {
        response.status should be(StatusCodes.OK)
      }
    }
  }
}
