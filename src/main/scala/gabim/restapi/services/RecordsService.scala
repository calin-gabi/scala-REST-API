package gabim.restapi.services

import java.sql.Date

import gabim.restapi.models.{RecordEntity, RecordEntityUpdate, UserEntity, UserResponseEntity}
import gabim.restapi.models.db.RecordEntityTable
import gabim.restapi.utilities.DatabaseService
import com.github.nscala_time.time.OrderingImplicits._
import org.joda.time.DateTime

import scala.concurrent.{ExecutionContext, Future}

class RecordsService(val databaseService: DatabaseService)(implicit executionContext: ExecutionContext) extends RecordEntityTable {

  import databaseService._
  import databaseService.driver.api._

  def getRecordById(id: Long) : Future[Option[RecordEntity]] = {
    db.run(records.filter(_.id === id).result.headOption)
  }

  implicit val dateTimeColumnType = MappedColumnType.base[DateTime, Date](
    d => Date.valueOf(d.toString("yyyy-MM-dd HH:mm")),
    d => new DateTime(d.toLocalDate())
  )

  def getRecordsByUserId(id: Long) : Future[Seq[RecordEntity]] =  db.run(records.filter(_.userId === id).sortBy(_.date.desc.nullsFirst).result)

  def createRecord(record: RecordEntity) : Future[RecordEntity] = {
    val newRecord: RecordEntity = RecordEntity(None, record.userId, record.date, record.description,
      record.amount, record.comment, Option(0))
    db.run(records returning records += newRecord)
  }

  def updateRecord(id: Long, recordUpdate: RecordEntityUpdate) : Future[Option[RecordEntity]] = {
    getRecordById(id).flatMap{
      case Some(record) =>
        val updatedRecord = recordUpdate.merge(record)
        db.run(records.filter(_.id === id).update(updatedRecord)).map(_ => Some(updatedRecord))
      case None => Future.successful(None)
    }
  }

  def deleteRecord(id: Long) : Future[Int] = db.run(records.filter(_.id === id).delete)

  def canUpdateRecords(user: UserResponseEntity, userId: Long) = {
    Seq("admin", "manager").contains(user.role) || user.id == userId
  }
  def canViewRecords(user: UserResponseEntity, userId: Long) = {
    Seq("admin", "manager").contains(user.role) || user.id == userId
  }
}
