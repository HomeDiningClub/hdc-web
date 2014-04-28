package controllers

import org.springframework.beans.factory.annotation.Autowired
import play.api.mvc._
import services.WorldService
import models.World
import org.springframework.stereotype.{Controller => SpringController}


@SpringController
class WorldController extends Controller {

  @Autowired
  var worldService: WorldService = _

  def index = Action {
    if (worldService.getNumberOfWorlds > 0) {
      worldService.deleteAllWorlds
    }

    worldService.makeSomeWorldsAndRelations()

    val allWorlds: List[World] = worldService.getAllWorlds
    var pathFromFirstToLast: List[World] = Nil

    if (!allWorlds.isEmpty) {
      val first: World = allWorlds.head
      val last: World = allWorlds.last
      pathFromFirstToLast = worldService.getWorldPath(first, last)
    }
    Ok(views.html.worlds.index.render(allWorlds, pathFromFirstToLast))
  }
}