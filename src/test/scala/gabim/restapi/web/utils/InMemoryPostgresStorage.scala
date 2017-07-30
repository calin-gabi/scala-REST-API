package gabim.restapi.web.utils

import de.flapdoodle.embed.process.runtime.Network._
import gabim.restapi.utilities.FlywayService
import ru.yandex.qatools.embed.postgresql.PostgresStarter
import ru.yandex.qatools.embed.postgresql.config.AbstractPostgresConfig.{Credentials, Net, Storage, Timeout}
import ru.yandex.qatools.embed.postgresql.config.PostgresConfig
import ru.yandex.qatools.embed.postgresql.distribution.Version

object InMemoryPostgresStorage {
  val dbHost = getLocalHost.getHostAddress
  val dbPort = 25535
  val dbName = "restapi"
  val dbUser = "sa"
  val dbPassword = "2vE7kG4fG.@w9T"
  val jdbcUrl = s"jdbc:postgresql://$dbHost:$dbPort/$dbName"

  lazy val dbProcess = {
    val psqlConfig = new PostgresConfig(
      Version.V9_5_0, new Net(dbHost, dbPort),
      new Storage(dbName), new Timeout(),
      new Credentials(dbUser, dbPassword)
    )
    val psqlInstance = PostgresStarter.getDefaultInstance
    val flywayService = new FlywayService(jdbcUrl, dbUser, dbPassword)

    val process = psqlInstance.prepare(psqlConfig).start()
    flywayService.dropDatabase()
    flywayService.migrateDatabaseSchema()
    process
  }
}
