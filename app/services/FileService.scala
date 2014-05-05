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

@Service
class FileService {

  @Autowired
  private var fileRepository: FileRepository = _

  private lazy val bucketStore: String = Play.application.configuration.getString("aws.s3bucket")
  lazy val S3Bucket = S3(bucketStore)

  def listAllFilesRawFromS3(): List[String] = {
    val result = Await.result(S3Bucket.list, 10 seconds)
    val returnRes: List[String] = result.map(item => item.name).toList
    returnRes
  }

  // Accepts ImageFile, ContentFile
  //def getFilesOfType[T](): List[T] = {
    //val dbRes = fileRepository.findAllBySchemaPropertyValue.as(ImageFile)
    //val result = Await.result(S3Bucket.list, 10 seconds)
    //val returnRes: List[String] = result.map(item => item.name).toList
    //returnRes
  //}


  def uploadFile(file: MultipartFormData.FilePart[TemporaryFile]): ContentFile = {
    val contentType = file.contentType.toString
    val filename = play.utils.UriEncoding.encodePathSegment(file.filename, "UTF-8").toLowerCase.replace("+", "-") // TODO - Improve file names
    var newFile: ContentFile = new ImageFile(filename)

    val uploadedFile: BucketFile = BucketFile(newFile.bucketDir + newFile.key, contentType, FileUtils.readAllBytes(file.ref.file))
    val result = S3Bucket.add(uploadedFile)

    result.map {
      unit =>
        Logger.info("Uploaded and saved file: " + newFile.bucketDir + newFile.key)
        saveToDB(newFile)
        newFile
    }
    .recover {
      case S3Exception(status, code, message, originalXml) => Logger.error("Error: " + message)
      case _ => Logger.error("Error: Cannot upload image.")
    }

    newFile = null
    newFile
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
