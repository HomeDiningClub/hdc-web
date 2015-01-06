package controllers.admin

import java.io.File

import org.springframework.beans.factory.annotation.Autowired
import play.api.Play
import play.api.mvc._
import services.ContentFileService
import org.springframework.stereotype.{Controller => SpringController}
import play.api.libs.Files.TemporaryFile
import securesocial.core.SecureSocial
import enums.{FileTypeEnums, RoleEnums}
import utils.Helpers
import utils.authorization.WithRole
import models.UserCredential
import constants.FlashMsgConstants
import java.util.UUID
import play.api.libs.json.{JsNull, Json}

@SpringController
class AdminFileController extends Controller with SecureSocial {

  @Autowired
  private var contentFileService: ContentFileService = _

  def editIndex = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request =>
    Ok(views.html.admin.file.index())
  }

  def add = SecuredAction(authorize = WithRole(RoleEnums.ADMIN))(parse.multipartFormData) { implicit request =>

      request.body.file("file").map {
        file =>
          // We have to do this here, since otherwise we have a lock on the file
          val fileName = file.filename
          val newFile = contentFileService.createTemporaryFile(fileName)
          val contentType = file.contentType
          file.ref.moveTo(newFile, true)

          contentFileService.uploadFile(newFile, fileName, contentType, Helpers.getUserFromRequest.get.objectId, FileTypeEnums.IMAGE, isAdminFile = true) match {
            case Some(value) => Redirect(controllers.admin.routes.AdminFileController.editIndex()).flashing(FlashMsgConstants.Success -> {"File uploaded successfully:" + value.name})
            case None => BadRequest(views.html.admin.file.index()).flashing(FlashMsgConstants.Error -> "Something went wrong during upload, make sure it is a valid file (jpg,png,gif) and is less than 2MB.")
          }
      }.getOrElse {
        BadRequest(views.html.admin.file.index()).flashing(FlashMsgConstants.Error -> "No file selected")
      }
  }

  def deleteImage(id: UUID) = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request =>
    val result = contentFileService.deleteFile(id)
    if(result)
      Redirect(controllers.admin.routes.AdminFileController.editIndex()).flashing(FlashMsgConstants.Success -> "File deleted")
    else
      BadRequest(views.html.admin.file.index()).flashing(FlashMsgConstants.Error -> "Cannot delete file an error occurred")
  }


//  def listS3Files(prefix: String = "") = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request =>
//    val futList: Option[List[String]] = Some(fileService.listFilesRawFromS3(prefix))
//    Ok(views.html.file.index(futList))
//  }

}
