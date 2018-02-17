package gabim.restapi.web

import akka.http.javadsl.model.StatusCodes
import akka.http.scaladsl.model.{HttpEntity, MediaTypes}
import akka.http.scaladsl.server.Route
import gabim.restapi.models.RecordEntity
import io.circe.Decoder.Result
import io.circe.{Decoder, Encoder, HCursor, Json}
import io.circe.generic.auto._
import io.circe.syntax._
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
    val testRecords = provisionRecordsList(testUsers)
    val route = httpService.recordsRouter.route
  }

  def jsonPrinter[A: Encoder](obj: A): String = obj.asJson.noSpaces

  "The records service " should {

    "on get: return records of selected user" in new managerContext {
      val testUser = testUsers(0)
      val authorization = "Token" -> testTokens.find(_.get.id === testUser.id.get).get.get.token.get
      val userId = testUser.id.get
      val url = s"/records/${userId}"
      Get(url) ~> addHeader(authorization._1, authorization._2) ~> route ~> check {
        response.status should be(StatusCodes.OK)
      }
    }

    "on post: insert a new record for a selected user" in new managerContext {
      val testUser = testUsers(0)
      val authorization = "Token" -> testTokens.find(_.get.id === testUser.id.get).get.get.token.get
      val userId = testUser.id.get
      var record: RecordEntity = testRecords.find(_.userId == userId).get
      val requestEntity = HttpEntity(MediaTypes.`application/json`, record.asJson.noSpaces)
      val url = s"/records/${userId}"
      Post(url, requestEntity) ~> addHeader(authorization._1, authorization._2) ~> route ~> check {
        response.status should be(StatusCodes.OK)
      }
    }

    "on patch: update a record for a selected user" in new managerContext {
      val testUser = testUsers(0)
      val authorization = "Token" -> testTokens.find(_.get.id === testUser.id.get).get.get.token.get
      val userId = testUser.id.get
      var recordId: Long = testRecords.find(_.userId == userId).get.id.get
      val requestEntity = HttpEntity(MediaTypes.`application/json`, s"""{"amount": 999}""")
      val url = s"/records/${userId}/${recordId}"
      Patch(url, requestEntity) ~> addHeader(authorization._1, authorization._2) ~> route ~> check {
        response.status should be(StatusCodes.CREATED)
      }
    }

    "on delete: delete a record for a selected user" in new managerContext {
      val testUser = testUsers(0)
      val authorization = "Token" -> testTokens.find(_.get.id === testUser.id.get).get.get.token.get
      val userId = testUser.id.get
      var recordId: Long = testRecords.find(_.userId == userId).get.id.get
      val url = s"/records/${userId}/${recordId}"
      Delete(url, recordId) ~> addHeader(authorization._1, authorization._2) ~> route ~> check {
        response.status should be(StatusCodes.ACCEPTED)
      }
    }
  }
}
