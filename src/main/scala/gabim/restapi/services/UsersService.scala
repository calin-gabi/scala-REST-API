package gabim.restapi.services

import gabim.restapi.models._
import gabim.restapi.models.db.{TokenEntityTable, UserEntityTable, UsersProfileEntityTable}
import gabim.restapi.utilities.DatabaseService

import scala.concurrent.{ExecutionContext, Future}
import com.github.t3hnar.bcrypt._
import org.joda.time.DateTime
import org.mindrot.jbcrypt.BCrypt

class UsersService(val databaseService: DatabaseService)(implicit executionContext: ExecutionContext) extends UserEntityTable with UsersProfileEntityTable with  TokenEntityTable{

  import databaseService._
  import databaseService.driver.api._

  def getUsers(): Future[Seq[UserEntity]] = db.run(users.result)

  def getUserById(id: Long): Future[Option[UserEntity]] = db.run(users.filter(_.id === id).result.headOption)

  def getUserByLogin(login: String): Future[Option[UserEntity]] = db.run(users.filter(_.username === login).result.headOption)

  def getUserProfileByToken(token: String): Future[Option[UserResponseEntity]] = {
    val q = for {
      tk <- tokens if tk.token === token
      (user, profile) <- users joinLeft usersProfiles on (_.id === _.user_id) if user.id === tk.userId
    } yield (user, profile)
    db.run(q.result.headOption).map{
      case Some((user, profile)) => Option(UserResponseEntity(user.id.get, user.username, user.role.get, Option(token), profile))
      case None => Option(UserResponseEntity(0, "anonymus", "", Option(""), None))
    }
  }

  def isAvailable(username: String): Future[String] = db.run(users.filter(_.username === username).result.headOption).map {
    case Some(user) => "false"
    case None => "true"
  }

  def createUser(user: UserEntity): Future[UserEntity] = {
    val hashPass = BCrypt.hashpw(user.password, generateSalt)
    println(user)
    val dbUser: UserEntity = UserEntity(None, user.username, hashPass, user.role.orElse(Option("user")), user.last_login,
      user.attempts.orElse(Option(0)), user.lockoutdate, user.twofactor.orElse(Option(false)),
      user.email, user.emailconfirmed.orElse(Option(false)), user.phone, user.phoneconfirmed.orElse(Option(false)),
      user.active.orElse(Option(true)), user.created.orElse(Option(new DateTime())), user.rev.orElse(Option(0)))
    db.run(users returning users += dbUser)
  }

  def updateUser(id: Long, userUpdate: UserEntityUpdate): Future[Option[UserEntity]] = getUserById(id).flatMap {
    case Some(user) =>
      val updatedUser = userUpdate.merge(user)
      db.run(users.filter(_.id === id).update(updatedUser)).map(_ => Some(updatedUser))
    case None => Future.successful(None)
  }

  def deleteUser(id: Long): Future[Int] = db.run(users.filter(_.id === id).delete)

  def canUpdateUsers(user: UserResponseEntity) = user.role == "admin"
  def canViewUsers(user: UserResponseEntity) = Seq("admin", "manager").contains(user.role)
}