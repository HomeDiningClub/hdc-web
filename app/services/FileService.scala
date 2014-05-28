package services

import org.springframework.beans.factory.annotation.Autowired
import repositories.FileRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import models.files._
import fly.play.s3.{BucketItem, S3Exception, BucketFile, S3}
import play.{Logger, Play}
import play.api.mvc.MultipartFormData
import play.api.libs.Files.TemporaryFile
import org.parboiled.common.FileUtils
import play.api.libs.concurrent._
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.util.{Success, Failure}
import java.util.UUID

@Service
class FileService {

  @Autowired
  private var fileRepository: FileRepository = _

  private lazy val bucketStore: String = Play.application.configuration.getString("aws.s3bucket")
  lazy val S3Bucket = S3(bucketStore)

  def listFilesRawFromS3(prefix: String = ""): List[String] = {
    val result = Await.result(S3Bucket.list(prefix), 10 seconds)
    val returnRes: List[String] = result.map(item => item.name).toList
    returnRes
  }

  // Accepts Unique ID, returns url of db node
  def getFileUrlByKey(key: UUID): String = {
    val dbRes = fileRepository.findAllBySchemaPropertyValue("key",key).single()
    dbRes.url
  }


  // Accepts ImageFile, ContentFile
  //def getFilesOfType[T](): List[T] = {
    //val dbRes = fileRepository.findAllBySchemaPropertyValue.as(ImageFile)
    //val result = Await.result(S3Bucket.list, 10 seconds)
    //val returnRes: List[String] = result.map(item => item.name).toList
    //returnRes
  //}

  // Uploads a file
  // Return URL of file if successful
  def uploadFile(file: MultipartFormData.FilePart[TemporaryFile]): String = {
    val contentType = file.contentType.toString
    val filename = play.utils.UriEncoding.encodePathSegment(file.filename, "UTF-8").toLowerCase.replace("+", "-") // TODO - Improve file names
    var newFile: ContentFile = new ImageFile(filename)
    val fileUrl = newFile.bucketDir + newFile.key
    newFile.url = fileUrl
    //newFile.OwnedBy = TODO: Add user connecting using SecureSocial
    val newFileResults = newFile

    val uploadedFile: BucketFile = BucketFile(fileUrl, contentType, FileUtils.readAllBytes(file.ref.file))
    val result = S3Bucket.add(uploadedFile)

    result.map {
      unitResponse =>
        Logger.info("Uploaded and saved file: " + fileUrl)
        saveToDB(newFileResults)
        newFile.url
    }
    .recover {
      case S3Exception(status, code, message, originalXml) => Logger.error("Error: " + message)
      case _ => Logger.error("Error: Cannot upload image.")
    }

    ""
  }

  def deleteFile(fileToDelete: ContentFile) {
    val result: Future[Unit] = Future {
      S3Bucket.remove(fileToDelete.key.toString)
    }
    val timeoutFuture = Promise.timeout("Delete failed, timeout occurred", 20.seconds)

    Future.firstCompletedOf(Seq(result, timeoutFuture)).map {
      unit => Logger.info("Deleted file: " + fileToDelete.key)
        deleteFromDB(fileToDelete)
    }
    .recover {
      case S3Exception(status, code, message, originalXml) => Logger.info("Error: " + message)
    }
  }


  @Transactional(readOnly = false)
  private def saveToDB(file: ContentFile) {
    fileRepository.save(file)
  }

  @Transactional(readOnly = false)
  private def deleteFromDB(file: ContentFile) {
    fileRepository.delete(file)
  }

}
