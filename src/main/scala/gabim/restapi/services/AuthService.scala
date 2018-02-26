package gabim.restapi.services

import gabim.restapi.models._
import gabim.restapi.models.db.TokenEntityTable
import gabim.restapi.utilities.DatabaseService

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import gabim.restapi.utilities.ClassConfig
import org.mindrot.jbcrypt.BCrypt
import authentikat.jwt._
import javax.xml.ws.http.HTTPException

class AuthService(val databaseService: DatabaseService)(usersService: UsersService)(implicit executionContext: ExecutionContext) extends TokenEntityTable {

  import databaseService._
  import databaseService.driver.api._

  val config = new ClassConfig

  def signIn(loginPassword: LoginPassword): Future[Option[TokenResponse]] = {
    db.run(users
            .filter(_.username === loginPassword.username)
            .result)
      .flatMap { users =>
        users
          .find(user => BCrypt.checkpw(loginPassword.password, user.password.get)) match {
          case Some(user) =>
            db.run(tokens
                    .filter(_.userId === user.id)
                    .result.headOption)
              .map {
                case Some(token) => Some(TokenResponse(token.token))
                case None        => Some(TokenResponse(Await.result(createToken(user), 5.seconds).token))
            }
          case None => Future.successful(None)
        }
    }
  }

  def getTokenByString(token: String): Future[Option[TokenEntity]] = db.run(tokens.filter(_.token === token).result.headOption)

  def signUp(newUser: UserEntity): Future[String] =
    usersService
      .createUser(newUser)
      .map(user => Await.result(createToken(user), 5.seconds).token)

  def authenticate(token: String): Future[String] =
    getTokenByString(token)
      .map {
        case Some(token) => token.token
        case None => "false"
      }

  def getAuthenticatedUser(token: String): Future[Option[UserResponseEntity]] =
    usersService.getUserProfileByToken(token)

  def createJwtToken(userEntity: UserEntity): String = {
    val claimsSet = JwtClaimsSet(Map("username" -> userEntity.username, "email" -> userEntity.email, "role" -> userEntity.role))
    JsonWebToken(JwtHeader(config.jwtHead), claimsSet, config.jwtSecret)
  }

  def validateJwtToken(jwtToken: String): Boolean = JsonWebToken.validate(jwtToken, "secretkey")

  def createToken(userEntity: UserEntity): Future[TokenEntity] =
    db.run(tokens returning tokens += TokenEntity(userId = userEntity.id, token = createJwtToken(userEntity)))

  def deleteToken(token: String): Future[Int] = db.run(tokens.filter(_.token === token).delete)
}
