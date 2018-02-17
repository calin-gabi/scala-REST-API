package gabim.restapi.services

import java.io.InputStreamReader
import java.util

import com.fasterxml.jackson.core.JsonFactory
import com.google.api.client.googleapis.apache.GoogleApacheHttpTransport
import gabim.restapi.models.db.{TokenEntityTable, UserEntityTable, UserOAuthEntityTable, UsersProfileEntityTable}
import gabim.restapi.utilities.{ClassConfig, Config, DatabaseService}

import scala.concurrent.{Await, ExecutionContext, Future}
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.googleapis.auth.oauth2._
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload
import com.google.api.services.oauth2.{Oauth2, Oauth2RequestInitializer}
import com.google.api.services.oauth2.model.Tokeninfo
import com.google.api.services.oauth2.model.Userinfoplus
import com.google.api.client.http.{HttpRequest, HttpRequestInitializer, HttpTransport, LowLevelHttpRequest}
import com.google.api.client.auth.oauth2.{BearerToken, Credential, TokenResponse}
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer
import gabim.restapi.models._
import org.joda.time.DateTime

import scala.util.Properties
import scala.concurrent.duration._

class OAuthService(val databaseService: DatabaseService)(usersService: UsersService, authService: AuthService)(implicit executionContext: ExecutionContext) extends UserOAuthEntityTable with UsersProfileEntityTable {

  import databaseService._
  import databaseService.driver.api._

  val config: Config = new ClassConfig

  val transport: HttpTransport = GoogleApacheHttpTransport.newTrustedTransport();
  val JSON_FACTORY = JacksonFactory.getDefaultInstance()
  val clientIDList: util.Collection[String] = util.Arrays.asList(config.googleClientID)

  def getGoogleCredentials(): GoogleCredential = {
    val scopes: util.Collection[String] = util.Arrays.asList("profile", "email")
    val clientSecrets: GoogleClientSecrets = loadGoogleCredentials()
    return new GoogleCredential.Builder()
      .setTransport(transport)
      .setJsonFactory(JSON_FACTORY)
      .setClientSecrets(clientSecrets.getDetails().getClientId(), clientSecrets.getDetails().getClientSecret())
      .build()
  }

  val credential = getGoogleCredentials()

  val googleOauth2: Oauth2 = new Oauth2.Builder(transport, JSON_FACTORY, credential)
    .setApplicationName("rest-api")
    .build()

  val googleTokenVerifier: GoogleIdTokenVerifier = new GoogleIdTokenVerifier.Builder(transport, JSON_FACTORY)
    .setAudience(clientIDList)
    .build()

  def loadGoogleCredentials(): GoogleClientSecrets = {
    val _inputStreamReader = new InputStreamReader(classOf[OAuthService].getResourceAsStream("/google-client-secrets.json"))
    GoogleClientSecrets.load(JSON_FACTORY, _inputStreamReader)
  }

  def verifyIdToken(idTokenString: String): GoogleIdToken = {
    val googleToken: GoogleIdToken = googleTokenVerifier.verify(idTokenString)
    if (googleToken == null) {
      println("Invalid token!")
    }
    googleToken
  }

  def getTokenInfo(accessToken: String): Option[Tokeninfo] = {
    try {
      val tokenInfo = googleOauth2.tokeninfo()
        .setAccessToken(accessToken)
        .execute()
      Option(tokenInfo)
    } catch {
      case x: Exception =>
        println(x)
        Option(new Tokeninfo)
    }
  }

  def createUserOAuth(userOAuth: UserOAuthEntity): Future[UserOAuthEntity] = db.run(usersOauth returning usersOauth += userOAuth)

  def loginOAuth(userOAuth: OAuthToken): Future[Option[UserResponseEntity]] = {
    val _tokenInfo = getTokenInfo(userOAuth.accessToken).get
    usersService.getUserByOAuth(_tokenInfo.getUserId(), userOAuth.oauthType)
      .flatMap {
        case None => signUpGoogle(userOAuth)
        case user => {
          val token = authService.createToken(user.get)
          authService.authenticate(token)
        }
      }
  }

  def createNewDbUser(userEntity: UserEntity, googleTokenPayload: GoogleIdToken.Payload): Unit = {
    val firstName: String = googleTokenPayload.get("given_name").toString()
    val lastName: String = googleTokenPayload.get("family_name").toString()
    val pictureUrl: String = googleTokenPayload.get("picture").toString()
    val newUserProfile: UserProfileEntity = UserProfileEntity(userEntity.id.get, Option(firstName), Option(lastName), Option(pictureUrl))
    usersService.createUserProfile(newUserProfile)
    val newUserOAuth: UserOAuthEntity = UserOAuthEntity(userEntity.id.get, googleTokenPayload.getUserId(), "google")
    createUserOAuth(newUserOAuth)
  }

  def signUpGoogle(oauthToken: OAuthToken): Future[Option[UserResponseEntity]] = {
    val googleTokenPayload: Payload = verifyIdToken(oauthToken.idToken).getPayload()
    val newUser: UserEntity = UserEntity(None, googleTokenPayload.getEmail(), Option(""), Option("user"), None, None, None, None,
      Option(googleTokenPayload.getEmail()), Option(true), None, None, Option(true), Option(new DateTime()), Option(0))
    val newDbUser: Future[UserEntity] = usersService.createUser(newUser)
    newDbUser.flatMap(userEntity => {
      createNewDbUser(userEntity, googleTokenPayload)
      val token = authService.createToken(userEntity)
      authService.authenticate(token)
    })
  }

  def signUpOAuth(oauthToken: OAuthToken): Future[Option[UserResponseEntity]] = {
    oauthToken.oauthType match {
      case "google" => signUpGoogle(oauthToken)
      case whoa => null
    }
  }
}