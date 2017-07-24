package gabim.restapi.models.db

import slick.driver.PostgresDriver.api._
import org.joda.time.DateTime
import com.github.tototoshi.slick.PostgresJodaSupport._

import gabim.restapi.models.RecordEntity
import gabim.restapi.utilities.DatabaseService

trait RecordEntityTable {

  protected val databaseService: DatabaseService
  import databaseService.driver.api._

  class Record(tag: Tag) extends Table[RecordEntity](tag, "records") {
    def id = column[Long]("id", O.PrimaryKey)
    def username = column[String]("username")
    def date = column[DateTime]("date")
    def description = column[String]("description")
    def amount = column[Double]("amount")
    def comment = column[String]("comment")
    def rev = column[Int]("rev")

    def * = (id, username, date, description, amount, comment, rev) <> ((RecordEntity.apply _).tupled, RecordEntity.unapply)
  }
}
