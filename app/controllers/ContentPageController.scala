package controllers

import javax.inject.{Named, Inject}

import org.springframework.stereotype.{Controller => SpringController}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{RequestHeader, Controller}
import org.springframework.beans.factory.annotation.Autowired
import securesocial.core.SecureSocial.RequestWithUser
import services.{MailService, ContentService}
import play.api.Logger

import models.UserCredential
import customUtils.security.SecureSocialRuntimeEnvironment
import traits.ProvidesAppContext

//@Named
class ContentPageController @Inject() (override implicit val env: SecureSocialRuntimeEnvironment) extends Controller with securesocial.core.SecureSocial with ProvidesAppContext {

  /*
  @Autowired
  private var contentService: ContentService = _
*/

  // Dynamic content
  def viewContentByName(contentName: String) = UserAwareAction { implicit request =>

    // Try getting the item using route-name, if failure show 404
    contentService.findContentPageByRoute(contentName: String) match {
      case Some(item) =>
        val childPages = contentService.getAndMapRelatedPages(item.objectId)
          Ok(views.html.contentcolumns.onecolumn(urlTitle = item.title, menuList = childPages, column1Header = item.title, column1PreAmble = item.preamble, column1Body = item.mainBody, currentUser = request.user))
      case None =>
        val errMess = "Cannot find content using name:" + contentName
        Logger.debug(errMess)
        NotFound(errMess)
    }
  }
}