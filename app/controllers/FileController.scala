package controllers

import org.springframework.beans.factory.annotation.Autowired
import play.api.mvc._
import services.{UserCredentialService, FileService}
import models.files._
import org.springframework.stereotype.{Controller => SpringController}
import play.api.libs.Files.TemporaryFile
import java.util.UUID
import securesocial.core.SecureSocial
import constants.{FlashMsgConstants, FileTransformationConstants}
import enums.FileTypeEnums
import presets.ImagePreSets

@SpringController
class FileController extends Controller with SecureSocial {

  @Autowired
  private var fileService: FileService = _

  def index = SecuredAction {
    Ok(views.html.edit.file.index())
  }

  def add = SecuredAction(parse.multipartFormData) {
    request =>
      request.body.file("file").map {
        file =>
          val tempFile: MultipartFormData.FilePart[TemporaryFile] = file

          fileService.uploadFile(tempFile, UserCredentialService.socialUser2UserCredential(request.user), FileTypeEnums.IMAGE, ImagePreSets.testImages) match {
            case Some(value) => Redirect(routes.FileController.index()).flashing(FlashMsgConstants.Success -> {"File uploaded successfully:" + value.name})
            case None => BadRequest(views.html.edit.file.index()).flashing(FlashMsgConstants.Error -> "Something went wrong during upload, make sure it is a valid file (jpg,png,gif) and is less than 2MB.")
          }
      }.getOrElse {
        BadRequest(views.html.edit.file.index()).flashing(FlashMsgConstants.Error -> "No file selected")
      }
      //Redirect(routes.FileController.index)
  }

  def deleteImage(key: UUID) = SecuredAction {
    val result = fileService.deleteFile(key)
    if(result)
      Redirect(routes.FileController.index()).flashing(FlashMsgConstants.Success -> "File deleted")
    else
      BadRequest(views.html.edit.file.index()).flashing(FlashMsgConstants.Error -> "Cannot delete file an error occurred")
  }


//  def listS3Files(prefix: String = "") = Action{
//    val futList: Option[List[String]] = Some(fileService.listFilesRawFromS3(prefix))
//    Ok(views.html.file.index(futList))
//  }

}
