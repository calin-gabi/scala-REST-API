package gabim.restapi.models

import slick.driver.PostgresDriver.api._
import org.joda.time.DateTime
import com.github.tototoshi.slick.PostgresJodaSupport._

case class UserEntity(
                       id: Option[Long] = None,
                       username: String,
                       password: String,
                       last_login: DateTime) {
  require(!username.isEmpty, "username.empty")
  require(!password.isEmpty, "password.empty")
}

case class UserEntityUpdate(
                             username: Option[String] = None,
                             password: Option[String] = None,
                             last_login: Option[DateTime] = None) {
  def merge(user: UserEntity): UserEntity = {
    UserEntity(
      user.id,
      username.getOrElse(user.username),
      password.getOrElse(user.password),
      last_login.getOrElse(user.last_login))
  }
}