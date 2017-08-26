package gabim.restapi.services

import java.io.InputStreamReader
import java.util

import com.fasterxml.jackson.core.JsonFactory
import com.google.api.client.googleapis.apache.GoogleApacheHttpTransport
import gabim.restapi.models.db.TokenEntityTable
import gabim.restapi.utilities.DatabaseService

import scala.concurrent.ExecutionContext
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

class OAuthService(val databaseService: DatabaseService)(usersService: UsersService)(implicit executionContext: ExecutionContext) extends TokenEntityTable {

  val clientIDList: util.Collection[String] = util.Arrays.asList("252899479655-aclf4njds8994sqe9q5trh7d5p5hivio.apps.googleusercontent.com")
  val scopes: util.Collection[String] = util.Arrays.asList("profile", "email")
  val transport: HttpTransport = GoogleApacheHttpTransport.newTrustedTransport();
  val JSON_FACTORY = JacksonFactory.getDefaultInstance()
  val clientSecrets: GoogleClientSecrets = loadGoogleCredentials()

  val verifier = new GoogleIdTokenVerifier.Builder(transport, JSON_FACTORY)
      .setAudience(clientIDList)
      .build()

  def loadGoogleCredentials(): GoogleClientSecrets = {
    val _inputStreamReader = new InputStreamReader(classOf[OAuthService].getResourceAsStream("/google-client-secrets.json"))
    val _clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, _inputStreamReader)
    _clientSecrets
  }

  def verifyIdToken(idTokenString: String): Boolean = {
    val googleToken = verifier.verify(idTokenString)
    if(googleToken != null) {
      val payload: Payload = googleToken.getPayload()
      val userId = payload.getSubject()
      println(userId)
      val email = payload.getEmail()
      println(email)
      true
    } else {
      println("Invalid token!")
      false
    }
  }

  def tokenInfo(accessToken: String): Boolean = {
    println(accessToken)
    val credential = new GoogleCredential.Builder()
        .setTransport(transport)
        .setJsonFactory(JSON_FACTORY)
        .setClientSecrets(clientSecrets.getDetails().getClientId(), clientSecrets.getDetails().getClientSecret())
        .build()
        .setAccessToken(accessToken)
    val oauth2: Oauth2 = new Oauth2.Builder(transport, JSON_FACTORY, credential).setApplicationName("rest-api")
      .build()
    val _tokenInfo = oauth2.tokeninfo()
      .setAccessToken(credential.getAccessToken())
      .execute()
    println(_tokenInfo)
    true
  }
}
