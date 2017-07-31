package gabim.restapi

import akka.actor.{ActorSystem, Props}
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.example.{MonitorActor, NodeConfig}
import gabim.restapi.http.HttpService
import gabim.restapi.services.{AuthService, RecordsService, UsersService}
import gabim.restapi.utilities.{ClassConfig, DatabaseService, FlywayService}

import scala.concurrent.ExecutionContext

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

  val flywayService = new FlywayService(config.jdbcUrl, config.dbUser, config.dbPassword)
  flywayService.migrateDatabaseSchema

  val databaseService = new DatabaseService(config.jdbcUrl, config.dbUser, config.dbPassword)

  val usersService = new UsersService(databaseService)
  val authService = new AuthService(databaseService)(usersService)
  val recordsService = new RecordsService(databaseService)

  val httpService = new HttpService(usersService, recordsService, authService)

  Http().bindAndHandle(httpService.routes, config.httpHost, config.httpPort)
}
