package gabim.restapi.services

import gabim.restapi.models._
import gabim.restapi.models.db.TokenEntityTable
import gabim.restapi.utilities.DatabaseService

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._
import org.mindrot.jbcrypt.BCrypt

class AuthService(val databaseService: DatabaseService)(usersService: UsersService)(implicit executionContext: ExecutionContext) extends TokenEntityTable {

  import databaseService._
  import databaseService.driver.api._

  def signIn(login: String, password: String): Future[Option[UserResponseEntity]] = {
    db.run(users.filter(u => u.username === login).result).flatMap { users =>
      users.find(user =>
        BCrypt.checkpw(password, user.password.get)
      ) match {
        case Some(user) => db.run(tokens.filter(_.userId === user.id).result.headOption).flatMap {
          case Some(token) => {
            authenticate(token.token)
          }
          case None        => {
            val token = createToken(user)
            authenticate(token)
          }
        }
        case None => Future.successful(None)
      }
    }
  }

  def getTokenUserByString(token: String): Future[Option[TokenEntity]] = db.run(tokens.filter(_.token === token).result.headOption)

  def signUp(newUser: UserEntity): Future[Option[UserResponseEntity]] = {
    usersService.createUser(newUser).flatMap(user => {
      val token = createToken(user)
      authenticate(token)
    })
  }

  def authenticate(token: String): Future[Option[UserResponseEntity]] = {
    usersService.getUserProfileByToken(token)
  }

  def createToken(user: UserEntity): String = {
    val token = TokenEntity(userId = user.id)
    db.run(tokens returning tokens += token)
    token.token
  }

  def deleteToken(token: String): Future[Int] = db.run(tokens.filter(_.token === token).delete)

}
