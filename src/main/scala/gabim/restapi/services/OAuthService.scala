package gabim.restapi.services

import java.io.InputStreamReader
import java.util

import com.fasterxml.jackson.core.JsonFactory
import com.google.api.client.googleapis.apache.GoogleApacheHttpTransport
import gabim.restapi.models.db.{TokenEntityTable, UserEntityTable, UserOAuthEntityTable, UsersProfileEntityTable}
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
import gabim.restapi.models._
import org.joda.time.DateTime
import sun.plugin.util.UserProfile

import scala.concurrent.duration._

class OAuthService(val databaseService: DatabaseService)(usersService: UsersService, authService: AuthService)(implicit executionContext: ExecutionContext) extends UserOAuthEntityTable with UsersProfileEntityTable {

  import databaseService._
  import databaseService.driver.api._

  val clientIDList: util.Collection[String] = util.Arrays.asList("252899479655-aclf4njds8994sqe9q5trh7d5p5hivio.apps.googleusercontent.com")
  val scopes: util.Collection[String] = util.Arrays.asList("profile", "email")
  val transport: HttpTransport = GoogleApacheHttpTransport.newTrustedTransport();
  val JSON_FACTORY = JacksonFactory.getDefaultInstance()
  val clientSecrets: GoogleClientSecrets = loadGoogleCredentials()
  val credential = new GoogleCredential.Builder()
    .setTransport(transport)
    .setJsonFactory(JSON_FACTORY)
    .setClientSecrets(clientSecrets.getDetails().getClientId(), clientSecrets.getDetails().getClientSecret())
    .build()
  val oauth2: Oauth2 = new Oauth2.Builder(transport, JSON_FACTORY, credential)
    .setApplicationName("rest-api")
    .build()

  val verifier = new GoogleIdTokenVerifier.Builder(transport, JSON_FACTORY)
    .setAudience(clientIDList)
    .build()

  def loadGoogleCredentials(): GoogleClientSecrets = {
    val _inputStreamReader = new InputStreamReader(classOf[OAuthService].getResourceAsStream("/google-client-secrets.json"))
    val _clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, _inputStreamReader)
    _clientSecrets
  }

  def verifyIdToken(idTokenString: String): GoogleIdToken = {
    val googleToken: GoogleIdToken = verifier.verify(idTokenString)
    if (googleToken != null) {
      val payload: Payload = googleToken.getPayload()
      val userId = payload.getSubject()
      val email = payload.getEmail()
    } else {
      println("Invalid token!")
    }
    googleToken
  }

  def tokenInfo(accessToken: String): Option[Tokeninfo] = {
    try {
      credential.setAccessToken(accessToken)
      val _tokenInfo = oauth2.tokeninfo()
        .setAccessToken(credential.getAccessToken())
        .execute()
      Option(_tokenInfo)
    } catch {
      case x: Exception =>
        println(x)
        Option(new Tokeninfo)
    }
  }

  def createUserOAuth(userOAuth: UserOAuthEntity): Future[UserOAuthEntity] = db.run(usersOauth returning usersOauth += userOAuth)

  def createUserProfile(userProfile: UserProfileEntity): Future[UserProfileEntity] = db.run(usersProfiles returning usersProfiles += userProfile)

  def loginOAuth(userOAuth: OAuthToken): Future[Option[UserResponseEntity]] = {
//    println(userOAuth)
    val _tokenInfo = tokenInfo(userOAuth.accessToken).get
    val _oauthUser = usersService.getUserByOAuth(_tokenInfo.getUserId(), userOAuth.oauthType)
    _oauthUser.flatMap(user => {
      user match {
        case None => signUpGoogle(userOAuth)
        case user => {
          authService.createToken(user.get)
        }
      }
    })
  }

  def signUpGoogle(oauthToken: OAuthToken): Future[Option[UserResponseEntity]] = {
    val tokeninfo: Payload = verifyIdToken(oauthToken.idToken).getPayload()
    val newUser: UserEntity = UserEntity(None, tokeninfo.getEmail(), Option(""), Option("user"), None, None, None, None,
      Option(tokeninfo.getEmail()), Option(true), None, None, Option(true), Option(new DateTime()), Option(0))
    val newDbUser: Future[UserEntity] = usersService.createUser(newUser)
    newDbUser.flatMap(userEntity => {
      val firstName: String = tokeninfo.get("given_name").toString()
//      println(tokeninfo.get("given_name"))
      val lastName: String = tokeninfo.get("family_name").toString()
      val pictureUrl: String = tokeninfo.get("picture").toString()
      val newUserProfile: UserProfileEntity = UserProfileEntity(userEntity.id.get, Option(firstName), Option(lastName), Option(pictureUrl))
      createUserProfile(newUserProfile)
      val newUserOAuth: UserOAuthEntity = UserOAuthEntity(userEntity.id.get, tokeninfo.getUserId(), "google")
      createUserOAuth(newUserOAuth)
      authService.createToken(userEntity)
    })
  }

  def loginGoogle(token: String): UserResponseEntity = {
    null
  }

  def signUpOAuth(oauthToken: OAuthToken): Future[Option[UserResponseEntity]] = {
    oauthToken.oauthType match {
      case "google" => signUpGoogle(oauthToken)
      case whoa => null
    }
  }

  //  def loginOAuth(oauthToken: OAuthToken): Future[Option[UserResponseEntity]] = {
  //    oauthToken.oauthType match {
  //      case "google" => Future {Option(loginGoogle(oauthToken))}
  //      case whoa => null
  //    }
  //  }
}