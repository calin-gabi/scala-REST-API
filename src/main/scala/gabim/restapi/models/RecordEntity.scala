package gabim.restapi.models

import slick.driver.PostgresDriver.api._
import org.joda.time.DateTime
import com.github.tototoshi.slick.PostgresJodaSupport._

case class RecordEntity(
                       id: Long,
                       username: String,
                       date: DateTime,
                       description: String,
                       amount: Double,
                       comment: String,
                       rev: Int
                        ) {

}

case class RecordEntityUpdate(
                               username: Option[String] = None,
                               date: Option[DateTime] = None,
                               description: Option[String] = None,
                               amount: Option[Double] = None,
                               comment: Option[String] = None,
                               rev: Option[Int] = None
                             ) {
  def merge(record: RecordEntity): RecordEntity = {
    RecordEntity(
      record.id,
      username.getOrElse(record.username),
      date.getOrElse(record.date),
      description.getOrElse(record.description),
      amount.getOrElse(record.amount),
      comment.getOrElse(record.comment),
      rev.getOrElse(record.rev)
    )
  }
}


