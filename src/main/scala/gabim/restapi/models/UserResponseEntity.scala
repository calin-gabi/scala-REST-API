package gabim.restapi.models

case class UserResponseEntity(
                             id: Long,
                             username: String,
                             fullname: String,
                             role: String,
                             token: Option[String],
                             profile: Option[UserProfileEntity]
                             ) {

}
