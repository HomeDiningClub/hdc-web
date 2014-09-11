package services

import org.springframework.beans.factory.annotation.Autowired
import repositories._
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import models.files._
import fly.play.s3.{BucketItem, S3Exception, BucketFile, S3}
import play.{Logger, Play}
import play.api.mvc.MultipartFormData
import play.api.libs.Files.TemporaryFile
import org.parboiled.common.FileUtils
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import java.util.{Locale, UUID}
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
import scala.Some
import fly.play.s3.BucketFile

@Service
class ContentFileService {

  @Autowired
  private var contentFileRepository: ContentFileRepository = _
  @Autowired
  private var fileTransformationRepository: FileTransformationRepository = _
  @Autowired
  private var bucketRepository: BucketRepository = _
  @Autowired
  private var userCredentialRepository: UserCredentialRepository = _

  // Just for testing, don't use in production
  def listFilesRawFromS3(prefix: String = ""): List[String] = {
    val result = Await.result(bucketRepository.S3Bucket.list(prefix), 10 seconds)
    val returnRes: List[String] = result.map(item => item.name).toList
    returnRes
  }

  // Accepts Unique ID, returns ContentFile is any
  def getFileByKey(objectId: UUID): Option[ContentFile] = {
    val results = contentFileRepository.findByobjectId(objectId) match {
      case null => None
      case unit => Some(unit)
    }
    results
  }


  // Get all files
  def getAllFiles: List[ContentFile] = {
    val dbRes = IteratorUtil.asCollection(contentFileRepository.findAll()).asScala
    val parsedList: List[ContentFile] = dbRes.toList
    parsedList
  }

  // Get all images
  def getAllImages: List[ContentFile] = {
    val results = contentFileRepository.findBybaseContentType(FileTypeEnums.IMAGE.toString).asScala match {
      case null => Nil
      case unit => unit.toList
    }
    results
  }

  // Get all videos
  def getAllVideos: List[ContentFile] = {
    val results = contentFileRepository.findBybaseContentType(FileTypeEnums.VIDEO.toString).asScala match {
      case null => Nil
      case unit => unit.toList
    }
    results
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
//  private def populateBucketUrls(filesToParse: Iterable[ContentFile]): List[ContentFile] = {
//    var parsedList: ListBuffer[ContentFile] = ListBuffer()
//
//    if(filesToParse != Nil || filesToParse != null)
//    {
//      for (file <- filesToParse) {
//        val basePath = getFilePath(file)
//        val fileExt = getFileExtension(file)
//
//        // Set the original url
//        file.basePath = basePath + fileExt
//        file.url = bucketRepository.S3Bucket.url(file.basePath)
//        for (fileTransformation <- file.fileTransformations.asScala)
//        {
//          val variationPath: String = getTransformationPath(fileTransformation)
//
//          // Set the transformation url
//          fileTransformation.basePath = basePath + variationPath + getFileExtension(fileTransformation)
//          fileTransformation.url = bucketRepository.S3Bucket.url(fileTransformation.basePath)
//        }
//        parsedList += file
//      }
//    }
//    parsedList.result()
//  }


  // Wrapper method for one item
  //  def populateBucketUrl(fileToParse: ContentFile): ContentFile = {
  //    val fileList: List[ContentFile] = List{fileToParse}
  //    val returnList = populateBucketUrls(fileList)
  //    returnList.head
  //  }



  def getBucketUrl(file: ContentFile): String = {
    val fileExt = getFileExtension(file)
    bucketRepository.S3Bucket.url(file.getBasePath + fileExt)
  }

  def getBucketUrl(fileTransformation: FileTransformation): String = {
    val variationPath = getTransformationPath(fileTransformation)
    bucketRepository.S3Bucket.url(fileTransformation.getOwnerFile.getBasePath + variationPath + getFileExtension(fileTransformation))
  }

  private def approveMimeType(file: MultipartFormData.FilePart[TemporaryFile], fileType: FileTypeEnums): Option[String] = {

    var contentType: Option[String] = None
    val fileContentType: Option[String] = file.contentType.map(_.toLowerCase(Locale.ENGLISH))

    if(fileType == FileTypeEnums.IMAGE){
      contentType = fileContentType match {
        case Some("image/jpeg") => Some("image/jpeg")
        case Some("image/png") => Some("image/png")
        case Some("image/gif") => Some("image/gif")
        case _ =>
          Logger.debug("Debug: Cannot upload image, file is of wrong mime-type")
          None
      }
    }else if (fileType == FileTypeEnums.VIDEO){
      contentType = fileContentType match {
        case Some("video/mpeg") => Some("video/mpeg")
        case Some("video/mp4") => Some("video/mp4")
        case Some("video/x-flv") => Some("video/x-flv")
        case _ =>
          Logger.debug("Debug: Cannot upload video, file is of wrong mime-type")
          None
      }
    }else{
      throw new IllegalArgumentException("Missing a correct fileType")
    }

    contentType
  }

  private def compareMimeType(file: MultipartFormData.FilePart[TemporaryFile], fileName: String): Boolean ={
    file.contentType.equals(MimeTypes.forFileName(fileName)) match {
      case true =>
        true
      case false =>
        Logger.error("Error: File and file-extensions has an different mime-types, aborting.")
        false
    }
  }

  // Gets the filename from a tempfile
  private def fetchFileName(file: MultipartFormData.FilePart[TemporaryFile]): String = {
    play.utils.UriEncoding.encodePathSegment(file.filename, "UTF-8")
  }

  // Gets the last instance of a "."
  private def fetchLastIndexOfDot(fileName: String): Int = {
    fileName.lastIndexOf('.') match {
      case -1 => fileName.length
      case integer => integer
    }
  }

  // Grab extensions, if no extension on file, add
  private def fetchExtension(fileName: String): String = {
    fileName.split('.').takeRight(1).headOption match {
      case None => "unknown"
      case Some(extension) => extension
    }
  }

  // Get filename without extension
  private def fetchFilenameWithoutExtension(fileName: String, lastIndexOfDot: Int): String = {
    fileName.substring(0, lastIndexOfDot) match {
      case "" => "no-filename"
      case filename => filename
    }
  }

  // Is the size too big, bail out
  // 1MB = 1048576
  // 2MB = 2097152
  // 3MB = 3145728
  // 4MB = 4194304
  private val OneMB: Long = 1048576
  private val TwoMB: Long = 2097152
  private val ThreeMB: Long = 3145728
  private val FourMB: Long = 4194304

  private def isFileSizeBiggerThan(file: MultipartFormData.FilePart[TemporaryFile], maxSize: Long): Boolean = {
    if(file.ref.file.length() > maxSize) {
      Logger.debug("Debug: Cannot upload image, image is too large, largest is " + maxSize + "kb")
      true
    }else{
      false
    }
  }

  // Get filename without extension
  private def cleanFilename(uncleanedFileName: String, uncleanedFileExt: String): String = {
    uncleanedFileName.toLowerCase.replaceAll("\\W+", "-") + "." + uncleanedFileExt.toLowerCase.replaceAll("\\W+", "")
  }

  // Parse file extension
  private def parseFileExtension(fileName: String): String = {
    fileName.split('.').drop(1).lastOption match {
      case None => "unknown"
      case Some(fileExt) => fileExt
    }
  }

  // Set filename & path
  // Goal is the following:
  //<userid>/<fileid>/<fileid>.<fileextension>
  private def createNewContentFile(fileName: String, fileExtension: String, contentType: Option[String], fileType: FileTypeEnums, user: UserCredential, isAdminFile: Boolean): ContentFile = {
    fileType match {
      case FileTypeEnums.IMAGE => new ContentFile(fileName,fileExtension,contentType.get,FileTypeEnums.IMAGE.toString,user,isAdminFile)
      case FileTypeEnums.VIDEO => new ContentFile (fileName, fileExtension, contentType.get, FileTypeEnums.VIDEO.toString,user,isAdminFile)
      case _ =>
        Logger.error("Error: No base file type specified on upload, must be either any of the FileTypeConstants")
        null
    }
  }

  // Uploads a file
  // Return ContentObject if found
  def uploadFile(file: MultipartFormData.FilePart[TemporaryFile], userObjectId: UUID, fileType: FileTypeEnums, fileTransformations: List[FileTransformation] = Nil, isAdminFile: Boolean = false): Option[ContentFile] = {

    // Prepare object
    var user: UserCredential = null

    // Is user logged in and can be found?
    if(userObjectId != null) {
      user = userCredentialRepository.findByobjectId(userObjectId)
    }

    if(user == null) {
      Logger.debug("Debug: Cannot upload image no logged in user, or user missing objectId.")
      return None
    }

    // Is the size too big, bail out
    if((isAdminFile && isFileSizeBiggerThan(file, FourMB)) || (!isAdminFile && isFileSizeBiggerThan(file, TwoMB))){
      return None
    }

    // Compare mime-type with approved types and fetch and return is so
    val contentType = approveMimeType(file,fileType)

    if(contentType.isEmpty)
      return None

    // Fetch the uploaded filename
    val uncleanedFullFileName = fetchFileName(file)

    // Find the last dot for extension, or if no extension use full length filename
    val lastIndexOfDot = fetchLastIndexOfDot(uncleanedFullFileName)

    // Grab extensions, if no extension on file, add
    val uncleanedFileExt = fetchExtension(uncleanedFullFileName)

    // Grab just the filename by removing the extension
    val uncleanedFileName = fetchFilenameWithoutExtension(uncleanedFullFileName,lastIndexOfDot)

    // Clean filename + extension build a new name
    val fileName = cleanFilename(uncleanedFileName, uncleanedFileExt)

    // If Filename and File-Mime type match set the file ending, else abort upload
    if(!compareMimeType(file,fileName)) {
      return None
    } else {

      // Parse file extension
      // Set filename & path
      // Goal is the following:
      //<userid>/<fileid>/<fileid>.<fileextension>
      val newFile = createNewContentFile(fileName,parseFileExtension(fileName),contentType,fileType,user,isAdminFile = isAdminFile)

      if(newFile == null)
        return None


      // Set path and Upload original
      val fileUrl = getFilePath(newFile,adminPath = isAdminFile) + getFileExtension(newFile)

      // Handle result
      val futureResult: Future[Option[ContentFile]] = doUpload(fileUrl,contentType.get,file.ref.file).map {
        unitResponse =>
          Logger.info("Uploaded and saved file: " + fileUrl)
          val savedFile = saveFile(newFile)
          Some(savedFile)
      }
      .recover {
        case S3Exception(status, code, message, originalXml) =>
          Logger.error("Error: " + message)
          return None
        case _ =>
          Logger.error("Error: Cannot upload file.")
          return None
      }

      val uploadResult = Await.result(futureResult, 20 seconds)

      if(uploadResult.nonEmpty)
      {
        // Successfully saved and uploaded, lets make the transforms if any
        // But only on the image objects
        if(fileTransformations.nonEmpty && newFile.baseContentType.equalsIgnoreCase(FileTypeEnums.IMAGE.toString)){
          val editedFile: ContentFile = uploadAndCreateTransformations(uploadResult.get, file.ref.file, fileTransformations, isAdminFile)
          return Some(editedFile)
        }

        return Some(newFile)
      }
      return None
    }
  }

  // Handles the basic upload to bucket
  private def doUpload(fileUrl: String, contentType: String, fileToUpload: File): Future[Unit] = {

    val uploadedFile: BucketFile = BucketFile(fileUrl, contentType, FileUtils.readAllBytes(fileToUpload))
    val result = bucketRepository.S3Bucket.add(uploadedFile)

    result
  }

  // Handle transformations
  private def uploadAndCreateTransformations(contentFile: ContentFile, fileToUpload: File, fileTransformations: List[FileTransformation], isAdminFile: Boolean = false): ContentFile = {

    var uploadResult: Option[ContentFile] = None

    if(fileTransformations.nonEmpty && contentFile != null)
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
        // Set the extension - All transforms should be saved as PNG
        transform.extension = "jpg"
        val fileUrl = getFilePath(contentFile, isAdminFile) + getTransformationPath(transform) + getFileExtension(transform)

        // Create a receiving file
        val transformedFile = new File("/tmp/" + fileUrl)

        // Do transformation!
        // We can add more transforms:
        // https://github.com/sksamuel/scrimage
        transform.transformationType match {
          case FileTransformationConstants.FIT => Image(fileToUpload).fit(transform.width, transform.height, color = Color.White).writer(Format.JPEG).withCompression(50).write(transformedFile)
          case FileTransformationConstants.SCALE => Image(fileToUpload).scale(transform.scale).writer(Format.JPEG).withCompression(50).write(transformedFile)
          case FileTransformationConstants.BOUND => Image(fileToUpload).bound(transform.width, transform.height).writer(Format.JPEG).withCompression(50).write(transformedFile)
          case FileTransformationConstants.COVER => Image(fileToUpload).cover(transform.width, transform.height).writer(Format.JPEG).withCompression(50).write(transformedFile)
          case _ => Image(fileToUpload).cover(transform.width, transform.height).writer(Format.JPEG).withCompression(50).write(transformedFile)
        }

        // Move to immutable
        val permTrans = transform


        // Upload it
        val futureResult: Future[Option[ContentFile]] = doUpload(fileUrl,permTrans.extension,transformedFile).map {
          unitResponse =>
            Logger.info("Uploaded and saved transformation of file: " + fileUrl)

            // Success upload, add / update this entry to db
            val savedTransform = saveTransformation(permTrans)

            // Add relation
            val editedFile = addTransformToFile(contentFile, savedTransform)

            // Save the file parent object
            val updatedParentFile = saveFile(editedFile)
            Some(updatedParentFile)
          }
          .recover {
            case S3Exception(status, code, message, originalXml) =>
              Logger.error("Error: " + message)
              None
            case _ =>
              Logger.error("Error: Cannot upload transformation of file.")
              None
        }

        uploadResult = Await.result(futureResult, 20 seconds)

        // Clean up local file
        transformedFile.delete()
      }
    }

    // Return the re-saved file or the original file
    uploadResult.getOrElse(contentFile)
  }


  // Returns file path using a file entry
  // Returns an String with full path
  def getFilePath(file: ContentFile, adminPath: Boolean = false): String = {

    var retString: String = ""

    if(adminPath){
      retString += "admin" + "/"
    }

    if(file.owner != null && file.owner.objectId != null)
      retString += file.owner.objectId + "/"
    else
      Logger.debug("Debug: Missing owner on content file")

    if(!file.objectId.toString.isEmpty)
      retString += file.objectId.toString + "/" + file.objectId.toString
    else
      Logger.debug("Debug: Missing key on content file")

    retString
  }

  // Returns the file extensions using a file entry
  // if empty, just returns an empty string
  private def getFileExtension(file: ContentFile): String = {
    if(!file.extension.isEmpty)
      return "." + file.extension
    else
      Logger.error("Error: Cannot return file extension, extension is missing.")

    ""
  }

  private def getFileExtension(transform: FileTransformation): String = {
    if(!transform.extension.isEmpty)
      return "." + transform.extension
    else
      Logger.error("Error: Cannot return file extension, extension is missing.")

    ""
  }


  // Returns a correct partial path from a FileTransform
  // If empty, just returns an empty string
  // Use this in combination with getFilePath & getFileExtension
  def getTransformationPath(fileTransformation: FileTransformation): String = {
    "-" + fileTransformation.name.toLowerCase + "-" + fileTransformation.width + "-" + fileTransformation.height
  }

  // Deletes any file, can be any that inherits from ContentFile
  // Returns false is failure, true if success
  def deleteFile(objectId: UUID): Boolean = {

    // Check file in DB
    val fileToDelete: Option[ContentFile] = getFileByKey(objectId) match {
      case Some(file) => Some(file)
      case None => None
    }
    if(fileToDelete.isEmpty)
      return false

    val fileTransformations: Iterable[FileTransformation] = IteratorUtil.asCollection(fileToDelete.get.fileTransformations).asScala

    // Remove file if found in DB
    // Also remove all its transforms
    val future: Future[Unit] = Future {
      bucketRepository.S3Bucket.remove(fileToDelete.get.getBasePath)
      if(fileTransformations.nonEmpty) {
        for (transform <- fileTransformations) {
          bucketRepository.S3Bucket.remove(transform.getBasePath)
        }
      }
    }

    val results: Future[Boolean] = future.map {
      unitResponse =>
        Logger.debug("Deleted file: " + fileToDelete.get.getBasePath)
        if(fileTransformations.nonEmpty) {
          for (transform <- fileTransformations) {
            deleteTransformation(transform)
          }
        }
        deleteFile(fileToDelete.get)
        true
    }
    .recover {
      case timeout:
        scala.concurrent.TimeoutException =>
          Logger.error("Error: Timeout before deleting files.")
          false
      case S3Exception(status, code, message, originalXml) =>
        Logger.error("Error: " + message)
        false
    }

    val deleteResults = Await.result(results, 10 seconds)

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

    deleteResults
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
  private def saveFile(file: ContentFile): ContentFile = {
    val returnFile: ContentFile = contentFileRepository.save(file)
    returnFile
  }

  @Transactional(readOnly = false)
  private def deleteFile(file: ContentFile) {
    contentFileRepository.delete(file)
  }

}
