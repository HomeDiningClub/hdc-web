package controllers.admin

import org.springframework.beans.factory.annotation.Autowired
import play.api.mvc._
import services.ContentFileService
import org.springframework.stereotype.{Controller => SpringController}

// Object just needs a default constructor
class AdminFileListController extends Controller { }

@SpringController
object AdminFileListController extends Controller {

  @Autowired
  private var fileService: ContentFileService = _

  def listAllImages = {
    val imageList = fileService.getAllImages
    views.html.admin.file.imagelist(imageList)
  }
}
