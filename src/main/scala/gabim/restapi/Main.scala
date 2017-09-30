package gabim.restapi

import akka.actor.{ActorSystem, Props}
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import gabim.restapi.http.HttpService
import gabim.restapi.services.{AuthService, OAuthService, RecordsService, UsersService}
import gabim.restapi.utilities.{ClassConfig, DatabaseService, FlywayService}
import java.sql.{Connection, DriverManager}

import scala.concurrent.{Await, ExecutionContext, Future}

object Main extends App{
/*  val nodeConfig = NodeConfig parse args

  // If a config could be parsed - start the system
  nodeConfig map { c =>
  }*/


  val config = new ClassConfig

  implicit val actorSystem = ActorSystem()
  implicit val executor: ExecutionContext = actorSystem.dispatcher
  implicit val log: LoggingAdapter = Logging(actorSystem, getClass)
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  // wait for the postgresql container to initialize
  // Thread.sleep(2000)
//  println(config.jdbcUrl + "/" + config.dbUser + "/" + config.dbPassword)
  val flywayService = new FlywayService(config.jdbcUrl, config.dbUser, config.dbPassword)

  val databaseService = new DatabaseService(config.jdbcUrl, config.dbUser, config.dbPassword)

  val usersService = new UsersService(databaseService)
  val authService = new AuthService(databaseService)(usersService)
  val oauthService = new OAuthService(databaseService)(usersService, authService)
  val recordsService = new RecordsService(databaseService)

  val httpService = new HttpService(usersService, recordsService, authService, oauthService)

  Http().bindAndHandle(httpService.routes, config.httpHost, config.httpPort)
}
