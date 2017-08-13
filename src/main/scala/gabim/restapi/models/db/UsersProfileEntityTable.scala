package gabim.restapi.models.db

import gabim.restapi.models.UserProfileEntity
import gabim.restapi.utilities.DatabaseService

trait UsersProfileEntityTable {

  protected val databaseService: DatabaseService
  import databaseService.driver.api._

  class UserProfile(tag: Tag) extends Table[UserProfileEntity](tag, "users_profile") {
    def userId = column[Long]("user_id", O.PrimaryKey)
    def first_name = column[Option[String]]("first_name")
    def last_name = column[Option[String]]("last_name")
    def picture_url = column[Option[String]]("picture_url")

    def * = (userId, first_name, last_name, picture_url) <> ((UserProfileEntity.apply _).tupled, UserProfileEntity.unapply)
  }

  protected val usersProfiles = TableQuery[UserProfile]
}
