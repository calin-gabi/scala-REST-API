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
    val dbUser: UserEntity = new UserEntity(None, user.username, hashPass, Option("user"), None, Option(0), None, Option(false),
      Option(user.username), Option(false), user.phone, Option(false), Option(true), None, Option(0))
    db.run(users returning users += dbUser)
  }

  def updateUser(id: Long, userUpdate: UserEntityUpdate): Future[Option[UserEntity]] = getUserById(id).flatMap {
    case Some(user) =>
      val updatedUser = userUpdate.merge(user)
      db.run(users.filter(_.id === id).update(updatedUser)).map(_ => Some(updatedUser))
    case None => Future.successful(None)
  }

  def deleteUser(id: Long): Future[Int] = db.run(users.filter(_.id === id).delete)

  def canUpdateUsers(user: UserEntity) = user.role == "admin"
  def canViewUsers(user: UserEntity) = user.role == "manager"

  def canUpdateRecords(user: UserEntity) = Seq("admin", "manager").contains(user.role)
  def canViewRecords(user: UserEntity) = Seq("admin", "manager", "user").contains(user.role)
}