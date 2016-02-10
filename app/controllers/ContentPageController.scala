package controllers

import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{RequestHeader, Controller}
import securesocial.core.SecureSocial
import services.{ContentService}
import play.api.Logger
import customUtils.security.SecureSocialRuntimeEnvironment

class ContentPageController @Inject() (override implicit val env: SecureSocialRuntimeEnvironment,
                                       val contentService: ContentService,
                                       val messagesApi: MessagesApi) extends Controller with SecureSocial with I18nSupport {

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