package gabim.restapi.services

import java.io.InputStreamReader
import java.util

import com.fasterxml.jackson.core.JsonFactory
import com.google.api.client.googleapis.apache.GoogleApacheHttpTransport
import gabim.restapi.models.db.{TokenEntityTable, UserEntityTable, UserOAuthEntityTable}
import gabim.restapi.utilities.DatabaseService

import scala.concurrent.{Await, ExecutionContext, Future}
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.googleapis.auth.oauth2._
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload
import com.google.api.services.oauth2.{Oauth2, Oauth2RequestInitializer}
import com.google.api.services.oauth2.model.Tokeninfo
import com.google.api.services.oauth2.model.Userinfoplus
import com.google.api.client.http.{HttpRequest, HttpRequestInitializer, HttpTransport, LowLevelHttpRequest}
import java.io.InputStreamReader

import com.google.api.client.auth.oauth2.{BearerToken, Credential, TokenResponse}
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer
import gabim.restapi.models.{OAuthToken, UserEntity, UserOAuthEntity, UserResponseEntity}
import org.joda.time.DateTime

import scala.concurrent.duration._

class OAuthService(val databaseService: DatabaseService)(usersService: UsersService, authService: AuthService)(implicit executionContext: ExecutionContext) extends UserOAuthEntityTable {

  import databaseService._
  import databaseService.driver.api._

  val clientIDList: util.Collection[String] = util.Arrays.asList("252899479655-aclf4njds8994sqe9q5trh7d5p5hivio.apps.googleusercontent.com")
  val scopes: util.Collection[String] = util.Arrays.asList("profile", "email")
  val transport: HttpTransport = GoogleApacheHttpTransport.newTrustedTransport();
  val JSON_FACTORY = JacksonFactory.getDefaultInstance()
  val clientSecrets: GoogleClientSecrets = loadGoogleCredentials()

  val googleVerifier = new GoogleIdTokenVerifier.Builder(transport, JSON_FACTORY)
      .setAudience(clientIDList)
      .build()

  def loadGoogleCredentials(): GoogleClientSecrets = {
    val _inputStreamReader = new InputStreamReader(classOf[OAuthService].getResourceAsStream("/google-client-secrets.json"))
    val _clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, _inputStreamReader)
    _clientSecrets
  }

  def verifyGoolgeIdToken(idTokenString: String): Boolean = {
    if(googleVerifier.verify(idTokenString) != null) {
      println("Token valid!")
      true
    } else {
      println("Invalid token!")
      false
    }
  }

  def googleTokenInfo(accessToken: String): Tokeninfo = {
    val credential = new GoogleCredential.Builder()
        .setTransport(transport)
        .setJsonFactory(JSON_FACTORY)
        .setClientSecrets(clientSecrets.getDetails().getClientId(), clientSecrets.getDetails().getClientSecret())
        .build()
        .setAccessToken(accessToken)
    val oauth2: Oauth2 = new Oauth2.Builder(transport, JSON_FACTORY, credential).setApplicationName("rest-api")
      .build()
    oauth2.tokeninfo()
      .setAccessToken(credential.getAccessToken())
      .execute()
  }

  def createUserOAuthEntity(userEntity: UserEntity, tokeninfo: Tokeninfo): Future[UserOAuthEntity] = {
    val userOAuth: UserOAuthEntity = UserOAuthEntity(userEntity.id.get, tokeninfo.getUserId(), "google")
    db.run(usersOauth returning usersOauth += userOAuth)
  }

  def createUserEntityFromTokenInfo (tokeninfo: Tokeninfo): Future[UserEntity] = {
    val newUser: UserEntity = UserEntity(None, tokeninfo.getEmail(), Option(""), Option("user"), None, None, None, None,
    Option(tokeninfo.getEmail()), Option(true), None, None, Option(true), Option(new DateTime()), Option(0))
    usersService.createUser(newUser)
  }

  def signUpGoogle(token: String): Future[Option[UserResponseEntity]] = {
    val tokeninfo = googleTokenInfo(token)
    createUserEntityFromTokenInfo(googleTokenInfo(token))
      .flatMap(userEntity => createUserOAuthEntity(userEntity, tokeninfo))
      .flatMap(userOAuthEntity => loginOAuth(userOAuthEntity))
  }

  def signUpOAuth(oauthToken: OAuthToken): Future[Option[UserResponseEntity]] = {
    oauthToken.oauthType match {
      case "google" => signUpGoogle(oauthToken.idToken)
      case whoa => null
    }
  }

  def loginOAuth(userOAuth: UserOAuthEntity): Future[Option[UserResponseEntity]] = {
    usersService.getUserByOAuth(userOAuth).flatMap { user =>
      val token = Await.result(authService.createToken(user.get), 5.seconds)
      authService.authenticate(token.token)
    }
  }

  def authenticateOAuth(oauthToken: OAuthToken): Future[Option[UserResponseEntity]] = {
    db.run(usersOauth
      .filter(_.oauthType === oauthToken.oauthType)
      .filter(_.oauthId === oauthToken.idToken)
      .result.headOption)
      .flatMap {
        case Some(userOAuth) => loginOAuth(userOAuth)
        case None => signUpOAuth(oauthToken)
      }
  }

}
