package gabim.restapi.services

import gabim.restapi.models.{UserEntity, UserEntityUpdate}
import gabim.restapi.models.db.UserEntityTable
import gabim.restapi.utilities.DatabaseService

import scala.concurrent.{ExecutionContext, Future}

import com.github.t3hnar.bcrypt._
import org.mindrot.jbcrypt.BCrypt

class UsersService(val databaseService: DatabaseService)(implicit executionContext: ExecutionContext) extends UserEntityTable {

  import databaseService._
  import databaseService.driver.api._

  def getUsers(): Future[Seq[UserEntity]] = db.run(users.result)

  def getUserById(id: Long): Future[Option[UserEntity]] = db.run(users.filter(_.id === id).result.headOption)

  def getUserByLogin(login: String): Future[Option[UserEntity]] = db.run(users.filter(_.username === login).result.headOption)

  def createUser(user: UserEntity): Future[UserEntity] = {
    val hashPass = BCrypt.hashpw(user.password, generateSalt)
    val dbUser: UserEntity = new UserEntity(None, user.username, hashPass, user.role, user.last_login, user.attempts,
      user.lockoutdate, user.twofactor, user.email, user.emailconfirmed, user.phone, user.phoneconfirmed,
      user.active, user.created, user.rev)
    db.run(users returning users += dbUser)
  }

  def updateUser(id: Long, userUpdate: UserEntityUpdate): Future[Option[UserEntity]] = getUserById(id).flatMap {
    case Some(user) =>
      val updatedUser = userUpdate.merge(user)
      db.run(users.filter(_.id === id).update(updatedUser)).map(_ => Some(updatedUser))
    case None => Future.successful(None)
  }

  def deleteUser(id: Long): Future[Int] = db.run(users.filter(_.id === id).delete)

  def canUpdateUsers(user: UserEntity) = user.role == Some("admin")
  def canViewUsers(user: UserEntity) = Seq(Some("admin"), Some("manager")).contains(user.role)
}