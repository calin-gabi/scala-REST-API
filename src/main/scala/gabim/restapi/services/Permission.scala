package gabim.restapi.services


object Permissions extends Enumeration {
  type Permission = Value

  val MANAGE_USERS = Value("MANAGE_USERS")
  val VIEW_USERS = Value("VIEW_USERS")

  val MANAGE_RECORDS = Value("MANAGE_RECORDS")
  val VIEW_RECORDS = Value("VIEW_RECORDS")
}

case class Permission(username: String, permission: Permissions.Permission)
