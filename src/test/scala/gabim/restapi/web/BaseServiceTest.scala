package gabim.restapi.web

import akka.http.scaladsl.testkit.ScalatestRouteTest
import de.heikoseeberger.akkahttpcirce.CirceSupport
import gabim.restapi.http.HttpService
import gabim.restapi.models.{RecordEntity, UserEntity}
import gabim.restapi.services.{AuthService, RecordsService, UsersService}
import gabim.restapi.utilities.DatabaseService
import org.scalatest._
import gabim.restapi.web.utils.InMemoryPostgresStorage._
import org.joda.time.DateTime
import sun.security.util.Password

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.Random


trait BaseServiceTest extends WordSpec with Matchers with ScalatestRouteTest with CirceSupport {

  dbProcess.getProcessId

  private val databaseService = new DatabaseService(jdbcUrl, dbUser, dbPassword)

  val usersService = new UsersService(databaseService)
  val authService = new AuthService(databaseService)(usersService)
  val recordsService = new RecordsService(databaseService)
  val httpService = new HttpService(usersService, recordsService, authService)

  def RandomString(size: Int): String = {
    val pass = Random.alphanumeric.take(10)
    pass.mkString("")}

  def randomPasswords(size: Int): Seq[String] = {
    (1 to size).map{ _ =>
      RandomString(10)
    }
  }

  def provisionUsersList(passwords: Seq[String], role: String): Seq[UserEntity] = {
    val savedUsers = passwords.map { pass =>
      UserEntity(Some(Random.nextLong()), "user_" + RandomString(3), pass, Option(role), None, Option(0), None, Option(false),
                              Option(""), Option(false), Option(""), Option(false), Option(true), None, Option(0))
    }.map(usersService.createUser)

    Await.result(Future.sequence(savedUsers), 5.seconds)
  }

  def provisionTokensForUsers(usersList: Seq[UserEntity]) = {
    val savedTokens = usersList.map(authService.createToken)
    Await.result(Future.sequence(savedTokens), 5.seconds)
  }

  def provisionRecordsList(usersList: Seq[UserEntity]) = {
    val savedRecords = usersList.map { user =>
      val record = RecordEntity(Some(Random.nextInt()), user.id.get, Option(new DateTime()), RandomString(20), 2, Option(RandomString(30)), Option(1))
      record
    }.map(recordsService.createRecord)

    val result = Await.result(Future.sequence(savedRecords), 5.seconds)
    result
  }
}