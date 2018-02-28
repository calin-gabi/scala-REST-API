package gabim.restapi.services

import java.sql.{Date, Timestamp}

import gabim.restapi.models.{RecordEntity, RecordEntityUpdate, UserEntity, UserResponseEntity}
import gabim.restapi.models.db.RecordEntityTable
import gabim.restapi.utilities.DatabaseService
import com.github.nscala_time.time.OrderingImplicits._
import org.joda.time.DateTime

import scala.concurrent.{ExecutionContext, Future}

class RecordsService(val databaseService: DatabaseService)(implicit executionContext: ExecutionContext) extends RecordEntityTable {

  import databaseService._
  import databaseService.driver.api._

  def getRecordById(id: Long) : Future[Option[RecordEntity]] = db.run(records.filter(_.id === id).result.headOption)

  implicit val dateTimeColumnType = MappedColumnType.base[DateTime, Timestamp](
    d => new Timestamp(d.getMillis),
    d => new DateTime(d.getTime())
  )

  def getRecordsByUserId(id: Long) : Future[Seq[RecordEntity]] =  db.run(records.filter(_.userId === id).sortBy(_.date.desc.nullsFirst).result)

  def createRecord(record: RecordEntity) : Future[RecordEntity] = {
    val newRecord: RecordEntity = RecordEntity(None, record.userId, record.date, record.description,
      record.amount, record.comment, Option(0))
    db.run(records returning records += newRecord)
  }

  def updateRecord(id: Long, recordUpdate: RecordEntityUpdate) : Future[Option[RecordEntity]] = {
    getRecordById(id)
      .flatMap{
        case Some(record) =>
          val updatedRecord = recordUpdate.merge(record)
          db.run(records.filter(_.id === id).update(updatedRecord)).map(_ => Some(updatedRecord))
        case None => Future.successful(None)
    }
  }

  def deleteRecord(id: Long) : Future[Int] = db.run(records.filter(_.id === id).delete)

  def filterRecord(userId: Long, startDate: DateTime, endDate: DateTime): Future[Seq[RecordEntity]] =
    db.run(records
            .filter(r => (r.userId === userId && r.date > startDate && r.date < endDate) )
      .sortBy(_.date.desc.nullsFirst).result)

  def canUpdateRecords(user: UserEntity, userId: Long) =
    Seq("admin", "manager").contains(user.role) || user.id.get == userId

  def canViewRecords(user: UserEntity, userId: Long) =
    Seq("admin", "manager").contains(user.role) || user.id.get == userId
}
