package services

import org.springframework.beans.factory.annotation.Autowired
import repositories.{FileTransformationRepository, BucketRepository, VideoRepository, ImageRepository}
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
import java.io.File
import com.sksamuel.scrimage._
import models.UserCredential
import constants.FileTransformationConstants
import enums.FileTypeEnums
import FileTypeEnums.FileTypeEnums

@Service
class FileService {

  @Autowired
  private var imageRepository: ImageRepository = _
  @Autowired
  private var videoRepository: VideoRepository = _
  @Autowired
  private var fileTransformationRepository: FileTransformationRepository = _
  @Autowired
  private var bucketRepository: BucketRepository = _

  private val keyConstant = "key"
  private val fileTransformNameConstant = "name"

  // Just for testing, don't use in production
  def listFilesRawFromS3(prefix: String = ""): List[String] = {
    val result = Await.result(bucketRepository.S3Bucket.list(prefix), 10 seconds)
    val returnRes: List[String] = result.map(item => item.name).toList
    returnRes
  }

  // Accepts Unique ID, returns ImageObject
  def getImageByKey(key: UUID): Option[ImageFile] = {
    val results = imageRepository.findAllBySchemaPropertyValue(keyConstant,key).single match {
      case unit => Some(populateBucketUrl(unit).asInstanceOf[ImageFile])
      case _ => None
    }
    results
  }

  // Accepts Unique ID returns VideoObject
  def getVideoByKey(key: UUID): Option[VideoFile] = {
    val results = videoRepository.findAllBySchemaPropertyValue(keyConstant,key).single match {
      case unit => Some(populateBucketUrl(unit).asInstanceOf[VideoFile])
      case _ => None
    }
    results
  }

  // Get all Images
  // TODO: Add user constraint
  def getImages(): List[ImageFile] = {
    val dbRes = IteratorUtil.asCollection(imageRepository.findAll()).asScala
    val parsedList: List[ImageFile] = populateBucketUrls(dbRes).asInstanceOf[List[ImageFile]]
    parsedList
  }

  // Get a single transform by name
  // Returns Option[FileTransformation]
  def getTransformationByName(name: String, file: ContentFile): Option[FileTransformation] = {
    if(!file.fileTransformations.isEmpty){
      for (transform: FileTransformation <- file.fileTransformations.asScala) {
        if(transform.name.equalsIgnoreCase(name))
          return Some(transform)
      }
    }
    None
  }


  // This method populates all urls for base image and all it's transforms
  private def populateBucketUrls(filesToParse: Iterable[ContentFile]): List[ContentFile] = {
    var parsedList: ListBuffer[ContentFile] = ListBuffer()

    for (file <- filesToParse) {
      val basePath = getFilePath(file)
      val fileExt = getFileExtension(file)

      // Set the original url
      file.basePath = basePath + fileExt
      file.url = bucketRepository.S3Bucket.url(file.basePath)
      for (fileTransformation <- file.fileTransformations.asScala)
      {
        val variationPath: String = getTransformationPath(fileTransformation)

        // Set the transformation url
        fileTransformation.basePath = basePath + variationPath + fileExt
        fileTransformation.url = bucketRepository.S3Bucket.url(fileTransformation.basePath)
      }
      parsedList += file
    }
    parsedList.result()
  }

  // Wrapper method for one item
  private def populateBucketUrl(fileToParse: ContentFile): ContentFile = {
    val fileList: List[ContentFile] = List{fileToParse}
    val returnList = populateBucketUrls(fileList)
    returnList.head
  }


  // Uploads a file
  // Return ContentObject if found
  def uploadFile(file: MultipartFormData.FilePart[TemporaryFile], user: UserCredential, fileType: FileTypeEnums, fileTransformations: List[FileTransformation] = Nil): Option[ContentFile] = {

    // Is user logged in?
    if(user == null && user.userId == null) {
      Logger.debug("Debug: Cannot upload image no logged in user.")
      return None
    }

    // Is the size too big, bail out
    // 1MB = 1048576
    // 2MB = 2097152
    // 3MB = 3145728
    // 4MB = 4194304
    if(file.ref.file.length() > 2097152) {
      Logger.debug("Debug: Cannot upload image, image is too large, largest image is 2MB.")
      return None
    }


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

      // Parse file extension
      val fileExtension = fileName.split('.').drop(1).lastOption match {
        case None => "unknown"
        case Some(fileExt) => fileExt
      }

      // Set filename & path
      // Goal is the following:
      //<userid>/<fileid>/<fileid>.<fileextension>
      val newFile = fileType match {
        case FileTypeEnums.IMAGE => new ImageFile(fileName,fileExtension,contentType,user)
        case FileTypeEnums.VIDEO => new VideoFile(fileName,fileExtension,contentType,user)
        case _ =>
          Logger.error("Error: No filetype specified on upload, must be either any of the FileTypeConstants")
          return null
      }
      val fileUrl = getFilePath(newFile) + getFileExtension(newFile)

      // Upload original
      val result = doUpload(fileUrl,contentType,file.ref.file)

      // Handle result
      result.map {
        unitResponse =>
          Logger.info("Uploaded and saved file: " + fileUrl)
          saveFile(newFile)
      }
        .recover {
          case S3Exception(status, code, message, originalXml) => Logger.error("Error: " + message)
          case _ => Logger.error("Error: Cannot upload file.")
      }

      // Successfully saved and uploaded, lets make the transforms if any
      // But only on the image objects
      if(!fileTransformations.isEmpty && newFile.isInstanceOf[ImageFile]){
        val editedFile: ContentFile = uploadAndCreateTransformations(newFile,file.ref.file,fileTransformations)
      }

      return Some(newFile)
    }
  }

  // Handles the basic upload to bucket
  private def doUpload(fileUrl: String, contentType: String, fileToUpload: File): Future[Unit] = {

    val uploadedFile: BucketFile = BucketFile(fileUrl, contentType, FileUtils.readAllBytes(fileToUpload))
    val result = bucketRepository.S3Bucket.add(uploadedFile)

    result
  }

  // Handle transformations
  private def uploadAndCreateTransformations(contentFile: ContentFile, fileToUpload: File, fileTransformations: List[FileTransformation]): ContentFile = {

    if(!fileTransformations.isEmpty && contentFile != null)
    {
      for(transform <- fileTransformations)
      {
        //var usingStoredTransform = false
        // Lookup earlier created transform if name is defined, this way all transforms with names are not unique:
        // This might cause confusion, since we use the stored values from the DB, not the ones passed in the method, however this is a small issue
//        val workingTransform: FileTransformation = fileTransformationRepository.findByName(transform.name) match {
//          case storedTransform: FileTransformation => {
//            Logger.debug("Debug: Transformation already exists, using the stored instance instead: " + storedTransform.name)
//            usingStoredTransform = true
//            storedTransform
//          }
//          case _ => transform
//        }

        // Build path to new file transformation
        val fileUrl = getFilePath(contentFile) + getTransformationPath(transform) + getFileExtension(contentFile)

        // Transform it
        val transformedFile = new File("/tmp/" + fileUrl)
        val transformType = "png"

        // We can add more transforms:
        // https://github.com/sksamuel/scrimage
        // Remember, here we might use the stored transformationType instead of the one entered by the developer
        transform.transformationType match {
          case FileTransformationConstants.FIT => Image(fileToUpload).fit(transform.width, transform.height, color = Color.White).write(transformedFile, Format.PNG)
          case FileTransformationConstants.SCALE => Image(fileToUpload).scale(transform.scale).write(transformedFile, Format.PNG)
          case FileTransformationConstants.COVER => Image(fileToUpload).cover(transform.width, transform.height).write(transformedFile, Format.PNG)
          case _ => Image(fileToUpload).cover(transform.width, transform.height).write(transformedFile, Format.PNG)
        }

        // Add the type
        transform.extension = transformType

        // Move to immutable
        val permTrans = transform

        // Upload it
        val results = doUpload(fileUrl,transformType,transformedFile).map {
          unitResponse =>
            Logger.info("Uploaded and saved transformation of file: " + fileUrl)

            // Success upload, add / update this entry to db
            val savedTransform = saveTransformation(permTrans)

            // Add relation
            val editedFile = addTransformToFile(contentFile, savedTransform)

            // Save the file parent object
            saveFile(editedFile)

          }
          .recover {
            case S3Exception(status, code, message, originalXml) =>
              Logger.error("Error: " + message)
              return null
            case _ =>
              Logger.error("Error: Cannot upload transformation of file.")
              return null
        }

        // Clean up local file
        transformedFile.delete()
      }
    }
    contentFile
  }


  // Returns file path using a file entry
  // Returns an String with full path
  private def getFilePath(file: ContentFile): String = {

    var retString: String = ""

    if(file.owner != null && !file.owner.userId.isEmpty())
      retString += file.owner.userId + "/"
    else
      Logger.debug("Debug: Missing owner on content file")

    if(!file.key.toString.isEmpty())
      retString += file.key.toString + "/" + file.key.toString
    else
      Logger.debug("Debug: Missing key on content file")

    retString
  }

  // Returns the file extensions using a file entry
  // if empty, just returns an empty string
  private def getFileExtension(file: ContentFile): String = {
    if(!file.extension.isEmpty())
      return "." + file.extension
    else
      Logger.error("Error: Cannot return file extension, extension if missing.")

    ""
  }

  // Returns a correct partial path from a FileTransform
  // If empty, just returns an empty string
  // Use this in combination with getFilePath & getFileExtension
  private def getTransformationPath(fileTransformation: FileTransformation): String = {
    "-" + fileTransformation.name.toLowerCase() + "-" + fileTransformation.width + "-" + fileTransformation.height
  }

  // Deletes any file, can be any that inherits from ContentFile
  // Returns false is failure, true if success
  def deleteFile(key: UUID): Boolean = {
    // Check file in DB
    val fileToDelete: ContentFile = getImageByKey(key) match {
      case Some(file) => file
      case None => return false
    }

    val fileTransformations: Iterable[FileTransformation] = IteratorUtil.asCollection(fileToDelete.fileTransformations).asScala

    // Remove file if found in DB
    // Also remove all its transforms
    val future: Future[Unit] = Future {
      bucketRepository.S3Bucket.remove(fileToDelete.basePath)
      if(!fileTransformations.isEmpty) {
        for (transform <- fileTransformations) {
          bucketRepository.S3Bucket.remove(transform.basePath)
        }
      }
    }

    val results = future.map {
      unitResponse =>
        Logger.info("Deleted file: " + fileToDelete.basePath.toString)
        if(!fileTransformations.isEmpty) {
          for (transform <- fileTransformations) {
            deleteTransformation(transform)
          }
        }
        deleteFile(fileToDelete)
        return true
    }
    .recover {
      case timeout:
        scala.concurrent.TimeoutException =>
          Logger.info("Error: Timeout before deleting files.")
          return false
      case S3Exception(status, code, message, originalXml) =>
        Logger.info("Error: " + message)
        return false
    }

    Await.result(future, 10.seconds)

//    result.onComplete {
//      case Success(_) =>
//        Logger.info("Deleted file: " + fileToDelete.basePath.toString)
//
//        for(transform <- fileTransformations) {
//          deleteTransformation(transform)
//        }
//        deleteFile(fileToDelete)
//
//        return true
//      case Failure(error) =>
//        Logger.info("Error: " + error)
//        return false
//    }

//    val waitedResult = Await.result(result, 10 seconds) : Unit
//    waitedResult

//    val timeoutFuture = Promise.timeout("Delete failed, timeout occurred", 20.seconds)
//    Future.firstCompletedOf(Seq(result, timeoutFuture)).map {
//      unitResponse => Logger.info("Deleted file: " + fileToDelete.basePath.toString)
//        for(transform <- fileTransformations) {
//          deleteTransformation(transform)
//        }
//        deleteFile(fileToDelete)
//        return true
//    }
//    .recover {
//      case S3Exception(status, code, message, originalXml) =>
//        Logger.info("Error: " + message)
//        return false
//    }

    false
  }


  @Transactional(readOnly = false)
  def addTransformToFile(file: ContentFile, transform: FileTransformation): ContentFile = {
    if(file != null && transform != null)
      file.fileTransformations.add(transform)

    file
  }

  @Transactional(readOnly = false)
  private def saveTransformation(transform: FileTransformation): FileTransformation = {
    val returnTransform: FileTransformation = fileTransformationRepository.save(transform)
    returnTransform
  }

  @Transactional(readOnly = false)
  private def deleteTransformation(transform: FileTransformation) {
    fileTransformationRepository.delete(transform)
  }


  @Transactional(readOnly = false)
  private def saveFile(file: ContentFile) {

    if(file.isInstanceOf[ImageFile])
      imageRepository.save(file.asInstanceOf[ImageFile])
    else if(file.isInstanceOf[VideoFile])
      videoRepository.save(file.asInstanceOf[VideoFile])

  }

  @Transactional(readOnly = false)
  private def deleteFile(file: ContentFile) {

    if(file.isInstanceOf[ImageFile])
      imageRepository.delete(file.asInstanceOf[ImageFile])
    else if(file.isInstanceOf[VideoFile])
      videoRepository.delete(file.asInstanceOf[VideoFile])
  }

}
