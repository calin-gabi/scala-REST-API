package gabim.restapi.http

import akka.http.scaladsl.server.Directives._
import gabim.restapi.http.routes.{AuthServiceRoute, UsersServiceRoute}
import gabim.restapi.services.{AuthService, UsersService}
import gabim.restapi.utilities.CORSConfig

import scala.concurrent.ExecutionContext

class HttpService(usersService: UsersService,
                  authService: AuthService
                 )(implicit executionContext: ExecutionContext) extends CORSConfig {

  val usersRouter = new UsersServiceRoute(authService, usersService)
  val authRouter = new AuthServiceRoute(authService)

  val routes =
    pathPrefix("") {
      corsHandler {
        usersRouter.route ~
        authRouter.route
      }
    }

}
