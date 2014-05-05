package controllers

import org.springframework.beans.factory.annotation.Autowired
import play.api.mvc._
import services.FileService
import models.files._
import org.springframework.stereotype.{Controller => SpringController}
import play.api.libs.Files.TemporaryFile

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

      Ok(views.html.file.index())
  }

  //def listAllImages = Action{
    //val futList: Option[List[String]] = Some(fileService.getFilesOfType[ImageFile]())
    //Ok(views.html.file.index(futList))
  //}

  def listAllFiles = Action{
    val futList: Option[List[String]] = Some(fileService.listAllFilesRawFromS3())
    Ok(views.html.file.index(futList))
  }

}
