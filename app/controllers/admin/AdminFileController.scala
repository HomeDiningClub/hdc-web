package controllers.admin

import java.io.File
import javax.inject.{Named, Inject}

import models.files.ContentFile
import org.springframework.beans.factory.annotation.Autowired
import play.api.Play
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import play.twirl.api.Html
import securesocial.core.SecureSocial.SecuredRequest
import services.ContentFileService
import org.springframework.stereotype.{Controller => SpringController}
import play.api.libs.Files.TemporaryFile

import enums.{FileTypeEnums, RoleEnums}
import customUtils.Helpers
import customUtils.authorization.WithRole
import models.UserCredential
import constants.FlashMsgConstants
import java.util.UUID
import play.api.libs.json.{JsNull, Json}
import customUtils.security.SecureSocialRuntimeEnvironment

//@Named
class AdminFileController @Inject() (override implicit val env: SecureSocialRuntimeEnvironment, val messagesApi: MessagesApi) extends Controller with securesocial.core.SecureSocial with I18nSupport {

  @Autowired
  private var contentFileService: ContentFileService = _

  @Autowired
  private var fileService: ContentFileService = _

  def editIndex = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request: SecuredRequest[AnyContent,UserCredential] =>
    Ok(views.html.admin.file.index(listOfImages = getAllImagesListAsHtml))
  }

  def add = SecuredAction(authorize = WithRole(RoleEnums.ADMIN))(parse.multipartFormData) { implicit request =>

      request.body.file("file").map {
        file =>
          // We have to do this here, since otherwise we have a lock on the file
          val fileName = file.filename
          val newFile = contentFileService.createTemporaryFile(fileName)
          val contentType = file.contentType
          file.ref.moveTo(newFile, true)

          contentFileService.uploadFile(newFile, fileName, contentType, request.user.objectId, FileTypeEnums.IMAGE, isAdminFile = true) match {
            case Some(value) => Redirect(controllers.admin.routes.AdminFileController.editIndex()).flashing(FlashMsgConstants.Success -> {"File uploaded successfully:" + value.name})
            case None => BadRequest(views.html.admin.file.index()).flashing(FlashMsgConstants.Error -> "Something went wrong during upload, make sure it is a valid file (jpg,png,gif) and is less than 2MB.")
          }
      }.getOrElse {
        BadRequest(views.html.admin.file.index()).flashing(FlashMsgConstants.Error -> "No file selected")
      }
  }

  def getAllImagesListAsHtml(implicit request: RequestHeader): Html = {
    views.html.admin.file.imagelist.render(fileService.getAllImages, request2Messages)
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
