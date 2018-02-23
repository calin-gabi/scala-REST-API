package gabim.restapi.http

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import gabim.restapi.http.routes.{AuthServiceRoute, OAuthServiceRoute, RecordsServiceRoute, UsersServiceRoute}
import gabim.restapi.services.{AuthService, OAuthService, RecordsService, UsersService}
import gabim.restapi.utilities.CORSConfig
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import gabim.restapi.models.UserEntity

import scala.concurrent.ExecutionContext

class HttpService(usersService: UsersService,
                  recordsService: RecordsService,
                  authService: AuthService,
                  oauthService: OAuthService
                 )(implicit executionContext: ExecutionContext) extends CORSConfig {

  val usersRouter = new UsersServiceRoute(authService, usersService)
  val authRouter = new AuthServiceRoute(authService)
  val oauthRouter = new OAuthServiceRoute(oauthService)
  val recordsRouter = new RecordsServiceRoute(authService, recordsService, usersService)
  def assets = pathPrefix("swagger") {
    getFromResourceDirectory("swagger") ~ pathSingleSlash(get(redirect("index.html", StatusCodes.PermanentRedirect))) }

  assets ~ new UsersServiceRoute(authService, usersService).route

  val routes = {
    corsHandler{
        usersRouter.route ~
          authRouter.route ~
          oauthRouter.route ~
          recordsRouter.route
    }
  }
}
