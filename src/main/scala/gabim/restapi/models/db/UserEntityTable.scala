package gabim.restapi.models.db

import gabim.restapi.models.{UserEntity, UserViewEntity}
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
    def password = column[Option[String]]("password")
    def role = column[Option[String]]("role")
    def last_login = column[Option[DateTime]]("last_login")
    def attempts = column[Option[Int]]("attempts")
    def lockoutdate = column[Option[DateTime]]("lockoutdate")
    def twoFactor = column[Option[Boolean]]("twofactor")
    def email = column[Option[String]]("email")
    def emailconfirmed = column[Option[Boolean]]("emailconfirmed")
    def phone = column[Option[String]]("phone")
    def phoneconfirmed = column[Option[Boolean]]("phoneconfirmed")
    def active = column[Option[Boolean]]("active")
    def created = column[Option[DateTime]]("created")
    def rev = column[Option[Long]]("rev")

    def * = (id, username, password, role, last_login, attempts, lockoutdate, twoFactor, email, emailconfirmed, phone, phoneconfirmed, active, created, rev) <> ((UserEntity.apply _).tupled, UserEntity.unapply)

  }

  protected val users = TableQuery[Users]

}
