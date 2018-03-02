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
import gabim.restapi.models
import gabim.restapi.models._
import org.joda.time.DateTime

import scala.concurrent.duration._

class OAuthService(val databaseService: DatabaseService)(usersService: UsersService, authService: AuthService)(implicit executionContext: ExecutionContext) extends UserOAuthEntityTable {

  import databaseService._
  import databaseService.driver.api._

  val clientIDList: util.Collection[String] = util.Arrays.asList("252899479655-aclf4njds8994sqe9q5trh7d5p5hivio.apps.googleusercontent.com")
  val scopes: util.Collection[String] = util.Arrays.asList("https://www.googleapis.com/auth/userinfo.profile", "https://www.googleapis.com/auth/userinfo.email")
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

  def verifyGoogleIdToken(idTokenString: String): Boolean = {
    if (googleVerifier.verify(idTokenString) != null) {
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

  def createUserEntityFromTokenInfo(tokeninfo: Tokeninfo, oAuthToken: OAuthToken): Future[UserEntity] = {
    val newUser: UserEntity = UserEntity(None, tokeninfo.getEmail(), oAuthToken.name, Option(""), Option("user"), None, None, None, None,
      Option(tokeninfo.getEmail()), Option(true), None, None, Option(true), Option(new DateTime()), Option(0))
    usersService.createUser(newUser)
  }

  def signUpGoogle(oAuthToken: OAuthToken): Future[gabim.restapi.models.TokenResponse] = {
    val tokeninfo: Tokeninfo = googleTokenInfo(oAuthToken.accessToken)
    createUserEntityFromTokenInfo(tokeninfo, oAuthToken)
      .flatMap(userEntity => createUserOAuthEntity(userEntity, tokeninfo))
      .flatMap(userOAuthEntity => loginOAuth(userOAuthEntity))
  }

  def signUpOAuth(oauthToken: OAuthToken): Future[gabim.restapi.models.TokenResponse] = {
    oauthToken.oauthType match {
      case "google" => signUpGoogle(oauthToken)
      case whoa => null
    }
  }

  def loginOAuth(userOAuth: UserOAuthEntity): Future[gabim.restapi.models.TokenResponse] = {
    usersService.getUserByOAuth(userOAuth).map(
      user => models.TokenResponse(Await.result(authService.createToken(user.get), 5.seconds).token)
    )
  }

  def authenticateOAuth(oauthToken: OAuthToken): Future[gabim.restapi.models.TokenResponse] = {
    db.run(usersOauth
      .filter(_.oauthType === oauthToken.oauthType)
      .filter(_.oauthId === oauthToken.uid)
      .result.headOption)
      .map {
        case Some(userOAuth) => Await.result(loginOAuth(userOAuth), 5.seconds)
        case None => Await.result(signUpOAuth(oauthToken), 5.seconds)
      }
  }
}
