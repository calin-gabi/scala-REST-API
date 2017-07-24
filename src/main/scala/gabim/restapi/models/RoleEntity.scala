package gabim.restapi.models

case class RoleEntity(
                      role: String,
                      description: String,
                      rev: Int
                      ) {

  require(!role.isEmpty,"role.isempty")
}

case class RoleEntityUpdate(
                             role: Option[String] = None,
                             description: Option[String] = None,
                             rev: Option[Int] = None) {
  def merge(nrole: RoleEntity): RoleEntity = {
    RoleEntity(
      role.getOrElse(nrole.role),
      description.getOrElse(nrole.description),
      rev.getOrElse(nrole.rev)
    )
  }
}
