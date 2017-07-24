package gabim.restapi.models.db

import gabim.restapi.models.RoleEntity
import gabim.restapi.utilities.DatabaseService

trait RoleEntityTable {

  protected val databaseService: DatabaseService
  import databaseService.driver.api._

  class Role(tag: Tag) extends Table[RoleEntity](tag, "roles") {
    def role = column[String]("role", O.PrimaryKey)
    def description = column[String]("description")
    def rev = column[Int]("rev")

    def * = (role, description, rev) <> ((RoleEntity.apply _).tupled, RoleEntity.unapply)
  }
}
