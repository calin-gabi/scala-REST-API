package gabim.restapi.models

case class UserOAuthEntity(
                            userId: Long,
                            oauthId: String,
                            oauthType: String
                          ) {
}

case class UserOAuthEntityUpdate(
                                  oauthId: Option[String] = None,
                                  oauthType: Option[String] = None
                                ) {
  def merge(userOAuth: UserOAuthEntity): UserOAuthEntity = {
    UserOAuthEntity(
      userOAuth.userId,
      oauthId.getOrElse(userOAuth.oauthId),
      oauthType.getOrElse(userOAuth.oauthType)
    )
  }
}

case class OAuthToken(
                       email: String,
                       idToken: String,
                       accessToken: String,
                       oauthType: String,
                       name: String,
                       image: String,
                       uid: String) {}