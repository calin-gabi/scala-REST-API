package gabim.restapi.models

case class TokenEntity(
                        id: Option[Long] = None,
                        userId: Option[Long],
                        token: String
                      )
