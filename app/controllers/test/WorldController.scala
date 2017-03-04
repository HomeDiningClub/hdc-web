package controllers.test

import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import securesocial.core.SecureSocial
import securesocial.core.SecureSocial.SecuredRequest
import services.WorldService
import models.{UserCredential, World}
import customUtils.authorization.WithRole
import enums.RoleEnums
import customUtils.security.SecureSocialRuntimeEnvironment

class WorldController @Inject() (implicit val env: SecureSocialRuntimeEnvironment,
                                 val worldService: WorldService,
                                 val messagesApi: MessagesApi) extends Controller with SecureSocial with I18nSupport {

  def index: Action[AnyContent] = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request: SecuredRequest[AnyContent,UserCredential] =>
    if (worldService.getNumberOfWorlds > 0) {
      worldService.deleteAllWorlds()
    }

    worldService.makeSomeWorldsAndRelations()

    val allWorlds: List[World] = worldService.getAllWorlds
    var pathFromFirstToLast: List[World] = Nil

    if (allWorlds.nonEmpty) {
      val first: World = allWorlds.head
      val last: World = allWorlds.last
      pathFromFirstToLast = worldService.getWorldPath(first, last)
    }
    Ok(views.html.worlds.index.render(allWorlds, pathFromFirstToLast, request, request2Messages))
  }
}