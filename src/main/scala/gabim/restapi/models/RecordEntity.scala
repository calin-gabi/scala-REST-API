package gabim.restapi.models

import slick.driver.PostgresDriver.api._
import org.joda.time.DateTime
import com.github.tototoshi.slick.PostgresJodaSupport._

case class RecordEntity(
                       id: Option[Long],
                       userId: Long,
                       date: DateTime,
                       description: String,
                       amount: Double,
                       comment: Option[String],
                       rev: Int
                        ) {
  require(amount >= 0, "Amount can not be negative!")
}

case class RecordEntityUpdate(
                               userId: Option[Long] = None,
                               date: Option[DateTime] = None,
                               description: Option[String] = None,
                               amount: Option[Double] = None,
                               comment: Option[String] = None
                             ) {
  def merge(record: RecordEntity): RecordEntity = {
    def newRev(rev: Int) = {
      rev + 1
    }
    RecordEntity(
      record.id,
      userId.getOrElse(record.userId),
      date.getOrElse(record.date),
      description.getOrElse(record.description),
      amount.getOrElse(record.amount),
      comment.orElse(record.comment),
      newRev(record.rev)
    )
  }
}


