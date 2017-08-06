package gabim.restapi.models

case class UserResponseEntity(
                             username: String,
                             token: Option[String],
                             profile: Option[UserProfileEntity]
                             ) {

}
