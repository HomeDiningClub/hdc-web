package controllers

import org.springframework.beans.factory.annotation.Autowired
import play.api.mvc._
import services.{UserCredentialService, FileService}
import models.files._
import org.springframework.stereotype.{Controller => SpringController}
import play.api.libs.Files.TemporaryFile
import java.util.UUID
import securesocial.core.SecureSocial
import constants.FileTransformationConstants

@SpringController
class FileController extends Controller with SecureSocial {

  @Autowired
  private var fileService: FileService = _


  def index = Action {
    Ok(views.html.file.index())
  }

  def add = SecuredAction(parse.multipartFormData) {
    request =>
      request.body.file("file").map {
        file =>
          val tempFile: MultipartFormData.FilePart[TemporaryFile] = file

          val fileTransforms: List[FileTransformation] = List[FileTransformation](
            new FileTransformation("myFittedImage", 400,400, FileTransformationConstants.FIT),
            new FileTransformation("myCoverImage", 300,300, FileTransformationConstants.COVER),
            new FileTransformation("myScaledImage", 0.5, FileTransformationConstants.SCALE)
          )

          fileService.uploadFile(tempFile, UserCredentialService.socialUser2UserCredential(request.user), fileTransforms)
      }.getOrElse {
        Redirect(routes.FileController.index()).flashing("error" -> "Missing file")
      }
      Redirect(routes.FileController.index())
  }

  def deleteImage(key: UUID) = SecuredAction {
    val result = fileService.deleteFile(key)
    if(result)
      Redirect(routes.FileController.index())
    else
      Redirect(routes.FileController.index()).flashing("error" -> "Cannot delete file an error occured")
  }


//  def listS3Files(prefix: String = "") = Action{
//    val futList: Option[List[String]] = Some(fileService.listFilesRawFromS3(prefix))
//    Ok(views.html.file.index(futList))
//  }

}
