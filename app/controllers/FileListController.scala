package controllers

import org.springframework.beans.factory.annotation.Autowired
import play.api.mvc._
import services.FileService
import org.springframework.stereotype.{Controller => SpringController}

// Object just needs a default constructor
class FileListController extends Controller { }

@SpringController
object FileListController extends Controller {

  @Autowired
  private var fileService: FileService = _

  def listAllImages = {
    val imagesList = fileService.getImages()
    views.html.file.imagelist(imagesList)
  }
}
