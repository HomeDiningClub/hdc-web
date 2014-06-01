package controllers

import org.springframework.beans.factory.annotation.Autowired
import play.api.mvc._
import services.FileService
import models.files._
import org.springframework.stereotype.{Controller => SpringController}
import play.api.libs.Files.TemporaryFile
import java.util.UUID

@SpringController
class FileController extends Controller {

  @Autowired
  private var fileService: FileService = _

  def index = Action {
    Ok(views.html.file.index())
  }

  def add = Action(parse.multipartFormData) {
    request =>
      request.body.file("file").map {
        file =>
          val filePerm: MultipartFormData.FilePart[TemporaryFile] = file
          fileService.uploadFile(filePerm)
      }.getOrElse {
        Redirect(routes.FileController.index()).flashing("error" -> "Missing file")
      }

      Redirect(routes.FileController.index())
  }

  def deleteImage(key: UUID) = Action {
    fileService.deleteFile(key)
    Ok(views.html.file.index())
  }


//  def listS3Files(prefix: String = "") = Action{
//    val futList: Option[List[String]] = Some(fileService.listFilesRawFromS3(prefix))
//    Ok(views.html.file.index(futList))
//  }

}
