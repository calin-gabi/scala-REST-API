package gabim.restapi.models

case class UserProfileEntity(
                            userId: Long,
                            first_name: Option[String],
                            last_name: Option[String],
                            picture_url: Option[String]
                            ) {
}
case class UserProfileEntityUpdate(
                                    username: Option[String] = None,
                                    first_name: Option[String] = None,
                                    last_name: Option[String] = None,
                                    picture_url: Option[String] = None) {

  def merge(userProfile: UserProfileEntity): UserProfileEntity = {
    UserProfileEntity(
      userProfile.userId,
      first_name.orElse(userProfile.first_name),
      last_name.orElse(userProfile.last_name),
      picture_url.orElse(userProfile.picture_url)
    )
  }
}
