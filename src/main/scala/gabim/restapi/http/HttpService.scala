package gabim.restapi.http

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import gabim.restapi.http.routes.{AuthServiceRoute, UsersServiceRoute}
import gabim.restapi.services.{AuthService, UsersService}
import gabim.restapi.utilities.CORSConfig
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._

import scala.concurrent.ExecutionContext

class HttpService(usersService: UsersService,
                  authService: AuthService
                 )(implicit executionContext: ExecutionContext) {

  val usersRouter = new UsersServiceRoute(authService, usersService)
  val authRouter = new AuthServiceRoute(authService)

  val routes = {
      usersRouter.route ~
      authRouter.route
  }
}
