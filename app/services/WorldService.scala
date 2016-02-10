package services

import javax.inject.{Named,Inject}

import org.neo4j.graphalgo.GraphAlgoFactory
import org.neo4j.graphdb._
import org.neo4j.helpers.collection.IteratorUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.neo4j.support.Neo4jTemplate
import org.springframework.stereotype.Service
import models.World
import repositories.WorldRepository
import scala.collection.mutable.ListBuffer
import scala.collection.JavaConverters._
import scala.List
import org.springframework.transaction.annotation.Transactional
import models.modelconstants.RelationshipTypesScala

@Service
class WorldService @Inject()(val template: Neo4jTemplate, val worldRepository: WorldRepository) {

  def getNumberOfWorlds: Long = worldRepository.count()

  def findByWorldByName(worldName: String): World = {
    worldRepository.findBySchemaPropertyValue("name", worldName)
  }

  @Transactional(readOnly = true)
  def getAllWorlds: List[World] = {
    val listOfWorlds: List[World] = IteratorUtil.asCollection(worldRepository.findAll()).asScala.toList
    listOfWorlds
  }

  @Transactional(readOnly = false)
  def makeSomeWorldsAndRelations(): List[World] = {
    var worlds : ListBuffer[World] = ListBuffer()
    worlds += createWorld("Mercury", 0)
    worlds += createWorld("Venus", 0)
    worlds += createWorld("Earth", 1, "Svenska,Turkiska")
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

    val myWorlds = worlds.result()
    // Just stupid code to make the next world rocket:able...
    myWorlds.zipWithIndex.foreach{
      case(w, i) =>
        if((worlds.length-2) > i)
          addRocketRouteTo(w,myWorlds(i+1))
        worldRepository.save(w)
    }
    myWorlds
  }

  @Transactional(readOnly = false)
  def deleteWorld(worldToDelete: World) {
    worldRepository.delete(worldToDelete)
  }

  @Transactional(readOnly = false)
  def deleteAllWorlds() {
    worldRepository.deleteAll()
  }

  @Transactional(readOnly = false)
  def addRocketRouteTo(thisWorld: World, otherWorld: World) {
    if(otherWorld != null && thisWorld != null)
      thisWorld.reachableByRocket.add(otherWorld)
  }

  @Transactional(readOnly = true)
  def getWorldPath(worldA: World, worldB: World): List[World] = {
    val pathExp: PathExpander[_] = PathExpanders.forTypeAndDirection(RelationshipTypesScala.REACHABLE_BY_ROCKET, Direction.OUTGOING)
    val path = GraphAlgoFactory.shortestPath(pathExp, 100)
      .findSinglePath(template.getNode(worldA.graphId), template.getNode(worldB.graphId))

    if (path == null) {
      return Nil
    }

    convertNodesToWorlds(path)
  }

  private def convertNodesToWorlds(list: Path): List[World] = {
    var convertList: ListBuffer[World] = ListBuffer()

    for (node <- list.iterator().asScala) {
      convertList += template.load(node, classOf[World])
    }
    convertList.result()
  }

  @Transactional(readOnly = false)
  private def createWorld(name: String, moons: Int, spokenLanguages: String = ""): World = {
    var newWorld: World = new World(name,moons)

    if(!spokenLanguages.isEmpty)
      newWorld.spokenLanguage = spokenLanguages

    newWorld = worldRepository.save(newWorld)
    newWorld
  }
}
