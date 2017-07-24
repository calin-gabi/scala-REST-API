package gabim.restapi.models.db

import gabim.restapi.models.UserEntity
import gabim.restapi.utilities.DatabaseService
import slick.driver.PostgresDriver.api._
import org.joda.time.DateTime
import com.github.tototoshi.slick.PostgresJodaSupport._

trait UserEntityTable {

  protected val databaseService: DatabaseService
  import databaseService.driver.api._

  class Users(tag: Tag) extends Table[UserEntity](tag, "users") {
    def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)
    def username = column[String]("username")
    def password = column[String]("password")
    def role = column[String]("role")
    def last_login = column[DateTime]("last_login")
    def attempts = column[Int]("attempts")
    def lockoutdate = column[DateTime]("lockoutdate")
    def twoFactor = column[Boolean]("twofactor")
    def email = column[String]("email")
    def emailconfirmed = column[Boolean]("emailconfirmed")
    def phone = column[String]("phone")
    def phoneconfirmed = column[Boolean]("phoneconfirmed")
    def active = column[Boolean]("active")
    def created = column[DateTime]("created")
    def rev = column[Long]("rev")

    def * = (id, username, password, role, last_login, attempts, lockoutdate, twoFactor, email, emailconfirmed, phone, phoneconfirmed, active, created, rev) <> ((UserEntity.apply _).tupled, UserEntity.unapply)
  }

  protected val users = TableQuery[Users]

}
