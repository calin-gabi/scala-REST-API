package gabim.restapi.models

import slick.driver.PostgresDriver.api._
import org.joda.time.DateTime
import com.github.tototoshi.slick.PostgresJodaSupport._

case class UserEntity(
                       id: Option[Long] = None,
                       username: String,
                       password: String,
                       role: String,
                       last_login: DateTime,
                       attempts: Int,
                       lockoutdate: DateTime,
                       twofactor: Boolean,
                       email: String,
                       emailconfirmed: Boolean,
                       phone: String,
                       phoneconfirmed: Boolean,
                       active: Boolean,
                       created: DateTime,
                       rev: Long) {
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
      role.getOrElse(user.role),
      last_login.getOrElse(user.last_login),
      attempts.getOrElse(user.attempts),
      lockoutdate.getOrElse(user.lockoutdate),
      twofactor.getOrElse(user.twofactor),
      email.getOrElse(user.email),
      emailconfirmed.getOrElse(user.emailconfirmed),
      phone.getOrElse(user.phone),
      phoneconfirmed.getOrElse(user.phoneconfirmed),
      active.getOrElse(user.active),
      created.getOrElse(user.created),
      rev.getOrElse(user.rev))
  }
}