package gabim.restapi.models.db

import gabim.restapi.models.{UserOAuthEntity}
import gabim.restapi.utilities.DatabaseService
import slick.driver.PostgresDriver.api._
import org.joda.time.DateTime
import com.github.tototoshi.slick.PostgresJodaSupport._

trait UserOAuthEntityTable extends UserEntityTable {

  protected val databaseService: DatabaseService
  import databaseService.driver.api._

  class UserOAuth(tag: Tag) extends Table[UserOAuthEntity](tag, "users_oauth"){
    def userId = column[Long]("user_id")
    def oauthId = column[String]("oauth_id")
    def oauthType = column[String]("oauth_type")

    def userFk = foreignKey("USER_FK", userId, users)(_.id.get, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

    def * = (userId, oauthId, oauthType) <> ((UserOAuthEntity.apply _).tupled, UserOAuthEntity.unapply)
  }
  protected val usersOauth = TableQuery[UserOAuth]
}
