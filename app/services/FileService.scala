package services

import org.springframework.beans.factory.annotation.Autowired
import repositories.{VideoRepository, ImageRepository}
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
import org.neo4j.helpers.collection.IteratorUtil
import scala.collection.JavaConverters._
import play.api.libs.MimeTypes
import scala.collection.mutable.ListBuffer

@Service
class FileService {

  @Autowired
  private var imageRepository: ImageRepository = _
  @Autowired
  private var videoRepository: VideoRepository = _

  private lazy val bucketStore: String = Play.application.configuration.getString("aws.s3bucket")
  lazy val S3Bucket = S3(bucketStore)

  private val keyConstant = "key"

  // Just for testing
  def listFilesRawFromS3(prefix: String = ""): List[String] = {
    val result = Await.result(S3Bucket.list(prefix), 10 seconds)
    val returnRes: List[String] = result.map(item => item.name).toList
    returnRes
  }

  // Accepts Unique ID
  def getImageByKey(key: UUID): Option[ImageFile] = {
    val dbRes = imageRepository.findAllBySchemaPropertyValue(keyConstant,key).single match {
      case unit => return Some(unit)
    }
    None
  }

  // Accepts Unique ID, returns url of db node
  def getVideoByKey(key: UUID): Option[VideoFile] = {
    val dbRes = videoRepository.findAllBySchemaPropertyValue(keyConstant,key).single match {
      case unit => return Some(unit)
    }
    None
  }

  // Get all Images
  // TODO: Add user constraint
  def getImages(): List[ImageFile] = {
    val dbRes = IteratorUtil.asCollection(imageRepository.findAll()).asScala

    var parsedList: ListBuffer[ImageFile] = ListBuffer()

    for(image <- dbRes) {
      image.url = S3Bucket.url(image.bucketDir + image.key.toString)
      parsedList += image
    }

    parsedList.result()
  }

  // Uploads a file
  // Return ContentObject if found
  def uploadFile(file: MultipartFormData.FilePart[TemporaryFile]): Option[ContentFile] = {

    // Check the Mime-type from the actual file
    val contentType = file.contentType match {
      case Some("js") => ""
      case Some("java") => ""
      case Some("cmd") => ""
      case Some("bat") => ""
      case Some("jar") => ""
      case Some("exe") => ""
      case Some(contentType) => contentType
      case None => ""
    }

    // Fetch the uploaded filename
    val uncleanedFullFileName: String = play.utils.UriEncoding.encodePathSegment(file.filename, "UTF-8")

    // Find the last dot for extension, or if no extension use full length filename
    val lastIndexOfDot = uncleanedFullFileName.lastIndexOf('.') match {
      case -1 => uncleanedFullFileName.length
      case integer => integer
    }

    // Grab extensions
    val uncleanedFileExt: String = uncleanedFullFileName.split('.').takeRight(1).headOption match {
      case None => ""  // TODO - Improve file extensions if none found
      case Some(extension) => extension
    }

    // Grab just the filename by removing the extension
    val uncleanedFileName: String = uncleanedFullFileName.substring(0, lastIndexOfDot) match {
        case "" => "nofilename"
        case filename => filename
      }

    // Clean filename + extension build a new name
    val fileName: String = uncleanedFileName.toLowerCase.replaceAll("\\W+", "-") + "." + uncleanedFileExt.toLowerCase.replaceAll("\\W+", "")

    // Get Mime-type as given by the filename
    val fileExtensionMimeType = MimeTypes.forFileName(fileName)

    // If they match set the file ending, else abort upload
    if (!file.contentType.equals(fileExtensionMimeType)) {

      Logger.error("Error: File has an invalid mime-type, aborting.")
      return None

    } else {

      val fileExtension = fileName.split('.').drop(1).lastOption match {
        case None => ""
        case Some(fileExt) => fileExt
      }
      val newFile: ImageFile = new ImageFile(fileName,fileExtension,contentType)
      val fileUrl = newFile.bucketDir + newFile.key

      //newFile.OwnedBy = TODO: Add user connecting using SecureSocial
      //newFile.url = fileUrl
      //val newFileResults = newFile

      val uploadedFile: BucketFile = BucketFile(fileUrl, contentType, FileUtils.readAllBytes(file.ref.file))
      val result = S3Bucket.add(uploadedFile)

      result.map {
        unitResponse =>
          Logger.info("Uploaded and saved file: " + fileUrl)
          saveToDB(newFile)
          return Some(newFile)
      }
        .recover {
        case S3Exception(status, code, message, originalXml) => Logger.error("Error: " + message)
        case _ => Logger.error("Error: Cannot upload image.")
      }

      None
    }
  }


  // Deletes any file, can be any that inherits from ContentFile
  // Returns false is failure, true if success
  def deleteFile(key: UUID): Boolean = {
    // Check file in DB
    val fileToDelete: ContentFile = getImageByKey(key) match {
      case Some(file) => file
      case None => return false
    }

    // Remove file if found in DB
    val result: Future[Unit] = Future {
      S3Bucket.remove(fileToDelete.key.toString)
    }
    val timeoutFuture = Promise.timeout("Delete failed, timeout occurred", 20.seconds)

    Future.firstCompletedOf(Seq(result, timeoutFuture)).map {
      unit => Logger.info("Deleted file: " + fileToDelete.key.toString)
        deleteFromDB(fileToDelete)
        true
    }
    .recover {
      case S3Exception(status, code, message, originalXml) => Logger.info("Error: " + message)
        false
    }
    false
  }


  @Transactional(readOnly = false)
  private def saveToDB(file: ContentFile) {

    if(file.isInstanceOf[ImageFile])
      imageRepository.save(file.asInstanceOf[ImageFile])
    else if(file.isInstanceOf[VideoFile])
      videoRepository.save(file.asInstanceOf[VideoFile])

  }

  @Transactional(readOnly = false)
  private def deleteFromDB(file: ContentFile) {

    if(file.isInstanceOf[ImageFile])
      imageRepository.delete(file.asInstanceOf[ImageFile])
    else if(file.isInstanceOf[VideoFile])
      videoRepository.delete(file.asInstanceOf[VideoFile])
  }

}
