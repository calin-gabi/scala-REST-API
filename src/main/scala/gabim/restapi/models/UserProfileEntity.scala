package gabim.restapi.models

case class UserProfileEntity(
                            username: String,
                            first_name: String,
                            last_name: String,
                            picture_url: String
                            ) {
  require(!username.isEmpty, "username.empty")
}
case class UserProfileEntityUpdate(
                                    username: Option[String] = None,
                                    first_name: Option[String] = None,
                                    last_name: Option[String] = None,
                                    picture_url: Option[String] = None) {

  def merge(userProfile: UserProfileEntity): UserProfileEntity = {
    UserProfileEntity(
      userProfile.username,
      first_name.getOrElse(userProfile.first_name),
      last_name.getOrElse(userProfile.last_name),
      picture_url.getOrElse(userProfile.picture_url)
    )
  }
}
