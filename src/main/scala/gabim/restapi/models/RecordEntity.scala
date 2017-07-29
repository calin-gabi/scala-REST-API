package gabim.restapi.models

import slick.driver.PostgresDriver.api._
import org.joda.time.DateTime
import com.github.tototoshi.slick.PostgresJodaSupport._

case class RecordEntity(
                       id: Long,
                       userId: Long,
                       date: DateTime,
                       description: String,
                       amount: Double,
                       comment: String,
                       rev: Int
                        ) {
  require(amount >= 0, "Amount can not be negative!")
}

case class RecordEntityUpdate(
                               userId: Option[Long] = None,
                               date: Option[DateTime] = None,
                               description: Option[String] = None,
                               amount: Option[Double] = None,
                               comment: Option[String] = None,
                               rev: Option[Int] = None
                             ) {
  def merge(record: RecordEntity): RecordEntity = {
    RecordEntity(
      record.id,
      userId.getOrElse(record.userId),
      date.getOrElse(record.date),
      description.getOrElse(record.description),
      amount.getOrElse(record.amount),
      comment.getOrElse(record.comment),
      rev.getOrElse(record.rev)
    )
  }
}


