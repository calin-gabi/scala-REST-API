package gabim.restapi.models

import slick.driver.PostgresDriver.api._
import org.joda.time.DateTime
import com.github.tototoshi.slick.PostgresJodaSupport._

case class UserEntity(
                       id: Option[Long] = None,
                       username: String,
                       password: String,
                       role: Option[String],
                       last_login: Option[DateTime],
                       attempts: Option[Int],
                       lockoutdate: Option[DateTime],
                       twofactor: Option[Boolean],
                       email: Option[String],
                       emailconfirmed: Option[Boolean],
                       phone: Option[String],
                       phoneconfirmed: Option[Boolean],
                       active: Option[Boolean],
                       created: Option[DateTime],
                       rev: Option[Long]) {
  require(!username.isEmpty, "username.empty")
  require(!password.isEmpty, "password.empty")
}

case class UserEntityUpdate(
                             username: Option[String] = None,
                             password: Option[String] = None,
                             role: Option[String] = None,
                             last_login: Option[DateTime] = None,
                             attempts: Option[Int] = None,
                             lockoutdate: Option[DateTime] = None,
                             twofactor: Option[Boolean] = None,
                             email: Option[String] = None,
                             emailconfirmed: Option[Boolean] = None,
                             phone: Option[String] = None,
                             phoneconfirmed: Option[Boolean] = None,
                             active: Option[Boolean] = None,
                             created: Option[DateTime] = None,
                             rev: Option[Long] = None) {
  def merge(user: UserEntity): UserEntity = {
    UserEntity(
      user.id,
      user.username,
      user.password,
      user.role,
      user.last_login,
      user.attempts,
      user.lockoutdate,
      user.twofactor,
      user.email,
      user.emailconfirmed,
      user.phone,
      user.phoneconfirmed,
      user.active,
      user.created,
      user.rev )
  }
}