package controllers

import org.springframework.beans.factory.annotation.Autowired
import play.api.mvc._
import services.ContentFileService
import org.springframework.stereotype.{Controller => SpringController}

// Object just needs a default constructor
class FileListController extends Controller { }

@SpringController
object FileListController extends Controller {

  @Autowired
  private var fileService: ContentFileService = _

  def listAllImages = {
    val imageList = fileService.getAllImages
    views.html.edit.file.imagelist(imageList)
  }
}
