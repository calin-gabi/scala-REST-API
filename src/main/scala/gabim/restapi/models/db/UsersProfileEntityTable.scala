package gabim.restapi.models.db

import gabim.restapi.models.UserProfileEntity
import gabim.restapi.utilities.DatabaseService

trait UsersProfileEntityTable {

  protected val databaseService: DatabaseService
  import databaseService.driver.api._

  class UserProfile(tag: Tag) extends Table[UserProfileEntity](tag, "user_profiles") {
    def username = column[String]("username", O.PrimaryKey)
    def first_name = column[String]("first_name")
    def last_name = column[String]("last_name")
    def picture_url = column[String]("picture_url")

    def * = (username, first_name, last_name, picture_url) <> ((UserProfileEntity.apply _).tupled, UserProfileEntity.unapply)
  }

  protected val usersProfiles = TableQuery[UserProfile]
}
