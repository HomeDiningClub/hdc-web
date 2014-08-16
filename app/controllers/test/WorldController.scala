package controllers.test

import org.springframework.beans.factory.annotation.Autowired
import play.api.mvc._
import services.WorldService
import models.World
import org.springframework.stereotype.{Controller => SpringController}
import utils.authorization.WithRole
import enums.RoleEnums
import securesocial.core.SecureSocial


@SpringController
class WorldController extends Controller with SecureSocial{

  @Autowired
  var worldService: WorldService = _

  def index = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request =>
    if (worldService.getNumberOfWorlds > 0) {
      worldService.deleteAllWorlds()
    }

    worldService.makeSomeWorldsAndRelations()

    val allWorlds: List[World] = worldService.getAllWorlds
    var pathFromFirstToLast: List[World] = Nil

    if (!allWorlds.isEmpty) {
      val first: World = allWorlds.head
      val last: World = allWorlds.last
      pathFromFirstToLast = worldService.getWorldPath(first, last)
    }
    Ok(views.html.worlds.index.render(allWorlds, pathFromFirstToLast, request))
  }
}