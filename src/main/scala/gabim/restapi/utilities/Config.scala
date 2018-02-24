package gabim.restapi.utilities

import com.typesafe.config.ConfigFactory

trait Config {
  private val config = ConfigFactory.load()
  private val httpConfig = config.getConfig("http")
  private val databaseConfig = config.getConfig("database")
  private val googleConfig = config.getConfig("googleOauth")
  private val jwtConfig = config.getConfig("jwt")

  val httpHost = httpConfig.getString("interface")
  val httpPort = httpConfig.getInt("port")

  val jdbcUrl = databaseConfig.getString("url")
  val dbUser = databaseConfig.getString("user")
  val dbPassword = databaseConfig.getString("password")

  val googleClientID = googleConfig.getString("clientID")

  val jwtHead = jwtConfig.getString("head")
  val jwtSecret = jwtConfig.getString("secretKey")
}

class ClassConfig extends Config