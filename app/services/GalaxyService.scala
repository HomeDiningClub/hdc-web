package services

import org.neo4j.graphalgo.GraphAlgoFactory
import org.neo4j.graphdb.Direction
import org.neo4j.graphdb.Node
import org.neo4j.graphdb.Path
import org.neo4j.helpers.collection
import org.neo4j.helpers.collection.IteratorUtil
import org.neo4j.kernel.Traversal
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.neo4j.support.Neo4jTemplate
import org.springframework.stereotype.Service
import models.World
import repositories.WorldRepository
import scala.collection.mutable.ListBuffer
import scala.collection.JavaConverters._
import scala.List
import org.springframework.data.neo4j.conversion.EndResult
import java.{util, lang}
import org.springframework.data.domain.{Page, Pageable, Sort}
import scala.language.implicitConversions
import scala.collection.JavaConversions._

@Service
class GalaxyService {

  @Autowired
  private var template: Neo4jTemplate = _

  @Autowired
  private var worldRepository: WorldRepository = _

  def getNumberOfWorlds(): Long = worldRepository.count()

  def getAllWorlds(): List[World] = {
    val listOfWorlds: List[World] = worldRepository.findAll().asScala.toList
    return listOfWorlds
  }

  def makeSomeWorldsAndRelations(): List[World] = {
    var worlds : ListBuffer[World] = ListBuffer()
    worlds += createWorld("Mercury", 0)
    worlds += createWorld("Venus", 0)
    worlds += createWorld("Earth", 1)
    worlds += createWorld("Mars", 2)
    worlds += createWorld("Jupiter", 63)
    worlds += createWorld("Saturn", 62)
    worlds += createWorld("Uranus", 27)
    worlds += createWorld("Neptune", 13)
    worlds += createWorld("Alfheimr", 0)
    worlds += createWorld("Midgard", 1)
    worlds += createWorld("Muspellheim", 2)
    worlds += createWorld("Asgard", 63)
    worlds += createWorld("Hel", 62)

    // Just stupid code to make the next world rocketable...
    worlds.zipWithIndex.foreach{case(w, i) => w.addRocketRouteTo(worlds(i + 1)); worldRepository.save(w) }

    // Kept this java-code for reference, does the same thing as the row above
    //for (i <- 0 until worlds.size - 1) {
    //  val world = worlds.get(i)
    //  world.addRocketRouteTo(worlds.get(i + 1))
    //  worldRepository.save(world)
    //}

    return worlds.result()
  }

  def getWorldPath(worldA: World, worldB: World): List[World] = {
    val path = GraphAlgoFactory.shortestPath(Traversal.expanderForTypes(World.RelTypes.REACHABLE_BY_ROCKET,
      Direction.OUTGOING)
      .add(World.RelTypes.REACHABLE_BY_ROCKET), 100)
      .findSinglePath(template.getNode(worldA.id), template.getNode(worldB.id))

    if (path == null) {
      return Nil
    }

    return convertNodesToWorlds(path)
  }

  private def convertNodesToWorlds(list: Path): List[World] = {
    var convertList: ListBuffer[World] = ListBuffer()
    for (node <- list.nodes()) {
      convertList += template.load(node, classOf[World])
    }
    return convertList.result()
  }

  private def createWorld(name: String, moons: Int): World = {
    worldRepository.save(new World(name, moons))
  }
}
