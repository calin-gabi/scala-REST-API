package gabim.restapi.services

import gabim.restapi.models._
import gabim.restapi.models.db.TokenEntityTable
import gabim.restapi.utilities.DatabaseService

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import org.mindrot.jbcrypt.BCrypt

class AuthService(val databaseService: DatabaseService)(usersService: UsersService)(implicit executionContext: ExecutionContext) extends TokenEntityTable {

  import databaseService._
  import databaseService.driver.api._

  def signIn(login: String, password: String): Future[Option[UserResponseEntity]] = {
    db.run(users
            .filter(_.username === login)
            .result)
      .flatMap { users =>
        users
          .find(user => BCrypt.checkpw(password, user.password.get)) match {
          case Some(user) =>
            db.run(tokens
                    .filter(_.userId === user.id)
                    .result.headOption)
              .flatMap {
                case Some(token) => usersService.getUserProfileByToken(token.token)
                case None        => usersService.getUserProfileByToken(Await.result(createToken(user), 5.seconds).token)
            }
          case None => Future.successful(None)
        }
    }
  }

  def getTokenUserByString(token: String): Future[Option[TokenEntity]] = db.run(tokens.filter(_.token === token).result.headOption)

  def signUp(newUser: UserEntity): Future[Option[UserResponseEntity]] =
    usersService
      .createUser(newUser)
      .flatMap(user => usersService.getUserProfileByToken(Await.result(createToken(user), 5.seconds).token))

  def authenticate(token: String): Future[Option[UserResponseEntity]] = usersService.getUserProfileByToken(token)

  def createToken(user: UserEntity): Future[TokenEntity] = db.run(tokens returning tokens += TokenEntity(userId = user.id))

  def deleteToken(token: String): Future[Int] = db.run(tokens.filter(_.token === token).delete)
}
