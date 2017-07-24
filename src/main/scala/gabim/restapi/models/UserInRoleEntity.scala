package gabim.restapi.models

case class UserInRoleEntity(
                             username: String,
                             role: String,
                             rev: Int){
  require(!username.isEmpty, "username.empty")
  require(!role.isEmpty, "role.empty")
}

case class UserInRoleEntityUpdate(
                                  username: Option[String] = None,
                                  role: Option[String] = None,
                                  rev: Option[Int] = None
                                  ) {
  def merge(userrole: UserInRoleEntity): UserInRoleEntity ={
    UserInRoleEntity(
      username.getOrElse(userrole.username),
      role.getOrElse(userrole.role),
      rev.getOrElse(userrole.rev)
    )
  }
}
