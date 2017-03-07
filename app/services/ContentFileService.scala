package services

import javax.inject.{Inject, Named}

import org.apache.commons.codec.digest.DigestUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.neo4j.support.Neo4jTemplate
import repositories._
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import models.files._
import play.api.{Application, Configuration, Logger, Play}
import java.util.{Locale, UUID}

import org.neo4j.helpers.collection.IteratorUtil
import traits.TransactionSupport

import scala.collection.JavaConverters._
import play.api.libs.MimeTypes
import java.io.{File, FileInputStream, IOException}

import models.UserCredential
import enums.FileTypeEnums
import FileTypeEnums.FileTypeEnums


class ContentFileService @Inject() (val template: Neo4jTemplate,
                                    val contentFileRepository: ContentFileRepository,
                                    val userCredentialRepository: UserCredentialRepository,
                                    implicit val conf: Configuration,
                                    implicit val application: Application,
                                    implicit val environment: play.api.Environment) extends TransactionSupport {

  def localWorkingDir: String = {
    var wrkDir = application.configuration.getString("hdc.wrkDir").getOrElse("/hdc-files/wrk-dir/")

    if (!wrkDir.endsWith("/")){
      wrkDir += "/"
    }
    wrkDir
  }

  def temporaryFileName: String = {
    UUID.randomUUID.toString
  }

  def createTemporaryFile(fileName: String): File = {
    new File(this.localWorkingDir + this.temporaryFileName + fetchExtension(fileName))
  }

  // Accepts Unique ID, returns ContentFile if any
  def getFileByKey(objectId: UUID): Option[ContentFile] = {
    val results = contentFileRepository.findByobjectId(objectId) match {
      case null => None
      case unit => Some(unit)
    }
    results
  }

  // Accepts Unique IDs, returns ContentFile if any
  def getFileByObjectIdAndOwnerId(objectId: UUID, userObjectId: UUID): Option[ContentFile] = {
    val results = contentFileRepository.findByobjectIdAndownerObjectId(objectId.toString, userObjectId.toString) match {
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


  def getCountOfAll: Int = withTransaction(template){
    contentFileRepository.getCountOfAll()
  }


  def getCountOfAllType(fileType: String): Int = withTransaction(template){
    contentFileRepository.getCountOfAllType(fileType)
  }

  def getImagesForUser(userObjectId: UUID): List[ContentFile] = {
    val results = contentFileRepository.findByownerObjectIdAndContentType(userObjectId.toString, FileTypeEnums.IMAGE.toString).asScala match {
      case null => Nil
      case unit => unit.toList
    }
    results
  }

  def getByListOfobjectIds(listOfObjectIds: Array[UUID]): List[ContentFile] = {
    // Map UUID -> String
    val listOfStrings: Array[String] = listOfObjectIds.map { item =>
      item.toString
    }

    val results = contentFileRepository.findByListOfobjectIds(listOfStrings).asScala match {
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

  private def approveMimeType(contentType: Option[String], fileType: FileTypeEnums): Option[String] = {

    var contentTypeReturn: Option[String] = None
    val fileContentType: Option[String] = contentType.map(_.toLowerCase(Locale.ENGLISH))

    if(fileType == FileTypeEnums.IMAGE){
      contentTypeReturn = fileContentType match {
        case Some("image/jpeg") => Some("image/jpeg")
        case Some("image/png") => Some("image/png")
        case Some("image/gif") => Some("image/gif")
        case _ =>
          Logger.debug("Debug: Cannot upload image, file is of wrong mime-type")
          None
      }
    }else if (fileType == FileTypeEnums.VIDEO){
      contentTypeReturn = fileContentType match {
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

    contentTypeReturn
  }

  private def compareMimeType(contentType: Option[String], fileName: String): Boolean ={
    contentType.equals(MimeTypes.forFileName(fileName)) match {
      case true =>
        true
      case false =>
        Logger.error("Error: File and file-extensions has an different mime-types, aborting.")
        false
    }
  }

  // Gets the filename from a tempfile
  private def fetchFileName(fileName: String): String = {
    play.utils.UriEncoding.encodePathSegment(fileName, "UTF-8")
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

  private def isFileSizeBiggerThan(file: File, maxSize: Long): Boolean = {
    if(file.length() > maxSize) {
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

  // Set filename & path
  // Goal is the following:
  //<userid>/<fileid>/<fileid>.<fileextension>
  private def createNewContentFile(fileName: String, fileExtension: String, contentType: Option[String], fileType: FileTypeEnums, user: UserCredential, isAdminFile: Boolean): ContentFile = {
    fileType match {
      case FileTypeEnums.IMAGE => new ContentFile(fileName, fileExtension, contentType.get, FileTypeEnums.IMAGE.toString, user, isAdminFile)
      case FileTypeEnums.VIDEO => new ContentFile(fileName, fileExtension, contentType.get, FileTypeEnums.VIDEO.toString, user, isAdminFile)
      case _ =>
        Logger.error("Error: No base file type specified on upload, must be either any of the FileTypeConstants")
        null
    }
  }

  // Uploads a file
  // Return ContentObject if found
  def uploadFile(file: File, fileName: String, contentType: Option[String], userObjectId: UUID, fileType: FileTypeEnums, isAdminFile: Boolean = false): Option[ContentFile] = {

    // Prepare object
    var user: UserCredential = null

    // Is user logged in and can be found?
    if(userObjectId != null) {
      user = userCredentialRepository.findByobjectId(userObjectId.toString)
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
    val approvedContentType = approveMimeType(contentType,fileType)

    if(approvedContentType.isEmpty)
      return None

    // Fetch the uploaded filename
    val uncleanedFullFileName = fetchFileName(fileName)

    // Find the last dot for extension, or if no extension use full length filename
    val lastIndexOfDot = fetchLastIndexOfDot(uncleanedFullFileName)

    // Grab extensions, if no extension on file, add
    val uncleanedFileExt = fetchExtension(uncleanedFullFileName)

    // Grab just the filename by removing the extension
    val uncleanedFileName = fetchFilenameWithoutExtension(uncleanedFullFileName,lastIndexOfDot)

    // Clean filename + extension build a new name
    val cleanedFileName = cleanFilename(uncleanedFileName, uncleanedFileExt)

    // If Filename and File-Mime type match set the file ending, else abort upload
    if(!compareMimeType(approvedContentType,cleanedFileName)) {
      return None
    } else {


      // Parse file extension
      // Set filename & path
      // Goal is the following:
      //<userid>/<fileid>/<fileid>.<fileextension>
      val newFile = createNewContentFile(cleanedFileName,fetchExtension(fileName),approvedContentType,fileType,user,isAdminFile = isAdminFile)

      if(newFile == null)
        return None

      // Set own filename
      //val storageFileName = generateFileName(newFile,adminPath = isAdminFile) + getFileExtension(newFile)

      // Set contentFile.objectId as metadata
      val metaData: scala.collection.mutable.Buffer[String] = scala.collection.mutable.Buffer(newFile.objectId.toString)

      if(isAdminFile)
      {
        metaData.append("admin")
      }

      return storeToRes(file, metaData, fetchExtension(cleanedFileName)) match {
      //storeToRes(file,storageFileName,fetchExtension(cleanedFileName)) match {
        case Some(storeId) =>

          // Connect new ContentFile to storage path
          newFile.setStoreId(storeId)
          val savedFile = saveFile(newFile)

          Logger.info("Uploaded and saved file: " + storeId)
          Some(savedFile)
        case _ =>
          Logger.error("Error: Cannot upload file.")
          None
      }
    }
  }


  // Resource
  private def storeToRes(fileToStore: File, metaData: Seq[String], fileExtension: String): Option[String] = {
  val res = new customUtils.res.api.Res()
    try {
      res.put(fileToStore, meta = metaData, extension = Some(fileExtension)) match {
      //utils.res.api.Res.put(fileToStore, filename = Some(fileNameToStore), extension = Some(fileExtension)) match {
        case id: String =>
          Some(id)
        case _ => None
      }
    }
    catch {
      case e: IOException => {
        Logger.error(e.toString)
        None
      }
    }
    finally {
      fileToStore.delete()
    }

  }

  // Returns filename using values from ContentFile
  def generateFileName(file: ContentFile, adminPath: Boolean): String = {

    var retString: String = ""

    if(adminPath){
      retString += "admin" + "-"
    }

    if(file.owner != null && file.owner.objectId != null)
      retString += file.owner.objectId.toString + "-"
    else
      Logger.debug("Debug: Missing owner on content file")

    if(!file.objectId.toString.isEmpty)
      retString += file.objectId.toString
    else
      Logger.debug("Debug: Missing key on content file")

    retString
  }

  // Returns a file name using hash from file
  def generateFileName(file: File, adminPath: Boolean): String = {
    var retString: String = ""

    if(adminPath){
      retString += "admin" + "-"
    }

    val fis = new FileInputStream(file)
    retString += DigestUtils.sha1Hex(fis)
    fis.close()

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

    // Delete from DB, then delete from Store - returns true or false
    deleteFileFromDB(fileToDelete.get) match {
      case true =>
        new customUtils.res.api.Res().delete(fileToDelete.get.getStoreId)
      case false =>
        false
    }

    // Deletes from store and then deletes the database-object returns true or false
/*    utils.res.api.Res.delete(fileToDelete.get.getStoreId) match {
      case true =>
        deleteFileFromDB(fileToDelete.get)
      case false =>
        false
    }*/

  }



  private def saveFile(file: ContentFile): ContentFile = withTransaction(template){
    file.getStoreId match {
      case null | "" =>
        throw new IllegalArgumentException("StoreID cannot be null or empty")
      case _ =>
        val returnFile: ContentFile = contentFileRepository.save(file)
        returnFile
    }
  }


  private def deleteFileFromDB(file: ContentFile): Boolean = withTransaction(template){
    try {
      contentFileRepository.delete(file)
      // This code works as well, however, it also has problems with the cached objects of Spring
      // contentFileRepository.deleteAllRelationsAndDelete(file)
    }catch {
      case e: Exception =>
        Logger.error("Could not delete image from DB: " + e.getMessage)
        return false
    }
    true
  }

}
