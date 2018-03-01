package gabim.restapi.services

import gabim.restapi.models._
import gabim.restapi.models.db.{TokenEntityTable, UserEntityTable, UserOAuthEntityTable, UsersProfileEntityTable}
import gabim.restapi.utilities.DatabaseService

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._
import com.github.t3hnar.bcrypt._
import org.joda.time.DateTime
import org.mindrot.jbcrypt.BCrypt

class UsersService(val databaseService: DatabaseService)(implicit executionContext: ExecutionContext) extends UserEntityTable with UsersProfileEntityTable with UserOAuthEntityTable with TokenEntityTable {

  import databaseService._
  import databaseService.driver.api._

  def getUsers(): Future[Seq[UserViewEntity]] = db.run(users
    .map(
      user => (user.id, user.username, user.fullname, user.role, user.email, user.phone, user.active)
    ).result.map(
    _.map(u => UserViewEntity(u._1, u._2, u._3, u._4, u._5, u._6, u._7))
  )
  )

  def getUserById(id: Long): Future[Option[UserEntity]] = db.run(users.filter(_.id === id).result.headOption)

  def getUserViewById(id: Long): Future[UserViewEntity] = db.run(users.filter(_.id === id).result.headOption)
    .map(user => UserViewEntity(user.get.id, user.get.username, user.get.fullname, user.get.role, user.get.email,user.get.phone, user.get.active))

  def getUserByLogin(login: String): Future[Option[UserEntity]] = db.run(users.filter(_.username === login).result.headOption)

  def getUserByOAuth(oauth: UserOAuthEntity): Future[Option[UserEntity]] = {
    val q = for {
      userO <- usersOauth filter (_.oauthId === oauth.oauthId) filter (_.oauthType === oauth.oauthType)
      user <- users filter (_.id === userO.userId)
    } yield (user)
    db.run(q.result.headOption)
  }

  def getUserByToken(token: String): Future[Option[UserEntity]] = {
    val q = for {
      userO <- tokens filter (_.token === token)
      user <- users filter (_.id === userO.userId)
    } yield (user)
    db.run(q.result.headOption)
  }

  def isAvailable(username: String): Future[String] = db.run(users.filter(_.username === username).result.headOption).map {
    case Some(user) => "false"
    case None => "true"
  }

  def getUserDefaultRole(): Future[String] = {
    db.run(users.filter(_.role === "admin").result.headOption)
      .map {
        case Some(user) => "user"
        case None => "admin"
      }
  }

  def createUser(user: UserEntity): Future[UserEntity] = {
    val hashPass = BCrypt.hashpw(user.password.get, generateSalt)
    val role = Await.result(getUserDefaultRole(), 5.seconds)
    val dbUser: UserEntity = UserEntity(None, user.username, user.fullname, Option(hashPass), user.role.orElse(Option(role)), user.last_login,
      user.attempts.orElse(Option(0)), user.lockoutdate, user.twofactor.orElse(Option(false)),
      user.email, user.emailconfirmed.orElse(Option(false)), user.phone, user.phoneconfirmed.orElse(Option(false)),
      user.active.orElse(Option(true)), user.created.orElse(Option(new DateTime())), user.rev.orElse(Option(0)))
    db.run(users returning users += dbUser)
  }

  def updateUser(id: Long, userUpdate: UserEntityUpdate): Future[Option[UserViewEntity]] = getUserById(id).flatMap {
    case Some(user) => {
      val updatedUser = userUpdate.merge(user)
      db.run(users
        .filter(_.id === id)
        .update(updatedUser))
        .map(_ =>
          Some(UserViewEntity(updatedUser.id, updatedUser.username, updatedUser.fullname, updatedUser.role, updatedUser.email, updatedUser.phone, updatedUser.active)))
    }
    case None => Future.successful(None)
  }

  def deleteUser(id: Long): Future[Int] = db.run(users.filter(_.id === id).delete)

  def canUpdateUsers(user: UserEntity) = user.role.get == "admin"

  def canViewUsers(user: UserEntity) = {
    Seq("admin", "manager").contains(user.role.get)
  }
}