package gabim.restapi.models.db

import gabim.restapi.models.UserInRoleEntity
import gabim.restapi.utilities.DatabaseService

trait UsersInRolesEntityTable {

  protected val databaseService: DatabaseService
  import databaseService.driver.api._

  class UserInRole(tag: Tag) extends Table[UserInRoleEntity](tag, "usersinroles") {
    def username = column[String]("username")
    def role = column[String]("role")
    def rev = column[Int]("rev")

    def * = (username, role, rev) <> ((UserInRoleEntity.apply _).tupled, UserInRoleEntity.unapply)
  }
}
