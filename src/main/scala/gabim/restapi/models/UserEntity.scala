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
      username.getOrElse(user.username),
      password.getOrElse(user.password),
      role.orElse(user.role),
      last_login.orElse(user.last_login),
      attempts.orElse(user.attempts),
      lockoutdate.orElse(user.lockoutdate),
      twofactor.orElse(user.twofactor),
      email.orElse(user.email),
      emailconfirmed.orElse(user.emailconfirmed),
      phone.orElse(user.phone),
      phoneconfirmed.orElse(user.phoneconfirmed),
      active.orElse(user.active),
      user.created,
      user.rev  )
  }
}

case class UsernameAvailable(
                             username: String
                           )

case class UserViewEntity (
                            id: Option[Long] = None,
                           username: String,
                           role: Option[String],
                           email: Option[String],
                           phone: Option[String],
                           active: Option[Boolean]) {

}