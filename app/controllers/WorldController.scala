package controllers

import org.springframework.beans.factory.annotation.Autowired
import play.api._
import play.api.mvc._
import services.GalaxyService
import models.World

/*
@org.springframework.stereotype.Controller
@Autowired
*/
class WorldController (galaxyService: GalaxyService) extends Controller {
/*
  def this() {
    println("constructing object")
  }
*/
  def index = Action {
    if (galaxyService.getNumberOfWorlds() == 0) {
      galaxyService.makeSomeWorldsAndRelations()
    }

    def allWorlds: List[World] = galaxyService.getAllWorlds()
    def first: World = allWorlds(0)
    def last: World = allWorlds.last
    def pathFromFirstToLast: List[World] = galaxyService.getWorldPath(first, last)

    Ok(views.html.worlds.index.render(allWorlds, pathFromFirstToLast))
  }
}