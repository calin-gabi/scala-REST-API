package gabim.restapi.services

import gabim.restapi.models.{RecordEntity, RecordEntityUpdate}
import gabim.restapi.models.db.RecordEntityTable
import gabim.restapi.utilities.DatabaseService

import scala.concurrent.{ExecutionContext, Future}

class RecordsService(val databaseService: DatabaseService)(implicit executionContext: ExecutionContext) extends RecordEntityTable {

  import databaseService._
  import databaseService.driver.api._

  def getRecordById(id: Long) : Future[Option[RecordEntity]] = db.run(records.filter(_.id === id).result.headOption)

  def getRecordsByUserId(id: Long) : Future[Seq[RecordEntity]] = db.run(records.filter(_.userId === id).result)

  def createRecord(record: RecordEntity) : Future[RecordEntity] = db.run(records returning records += record)

  def updateRecord(id: Long, recordUpdate: RecordEntityUpdate) : Future[Option[RecordEntity]] = getRecordById(id).flatMap{
    case Some(record) =>
      val updatedRecord = recordUpdate.merge(record)
      db.run(records.filter(_.id === id).result.headOption)
    case None => Future.successful(None)
  }

  def deleteRecord(id: Long) : Future[Int] = db.run(records.filter(_.id === id).delete)
}
