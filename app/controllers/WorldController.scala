package controllers

import org.springframework.beans.factory.annotation.Autowired
import play.api._
import play.api.mvc._
import services.GalaxyService
import models.World
import org.springframework.stereotype.{Controller => SpringController}


@SpringController
class WorldController extends Controller {

  @Autowired
  var galaxyService: GalaxyService = _

  def index = Action {
    if (galaxyService.getNumberOfWorlds() == 0) {
      galaxyService.makeSomeWorldsAndRelations()
    }

    val allWorlds: List[World] = galaxyService.getAllWorlds()
    var pathFromFirstToLast: List[World] = Nil

    if (!allWorlds.isEmpty) {
      val first: World = allWorlds.head
      val last: World = allWorlds.last
      pathFromFirstToLast = galaxyService.getWorldPath(first, last)
    }
    Ok(views.html.worlds.index.render(allWorlds, pathFromFirstToLast))
  }
}