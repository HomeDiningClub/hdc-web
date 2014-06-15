package services

import org.neo4j.graphalgo.GraphAlgoFactory
import org.neo4j.graphdb._
import org.neo4j.helpers.collection.IteratorUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.neo4j.support.Neo4jTemplate
import org.springframework.stereotype.Service
import models.content._
import scala.collection.mutable.ListBuffer
import scala.collection.JavaConverters._
import scala.List
import models.enums.RelationshipTypesEnums
import org.springframework.transaction.annotation.Transactional
import org.neo4j.graphdb.index.Index
import repositories.ContentRepository

@Service
class ContentService {

  @Autowired
  private var template: Neo4jTemplate = _

  @Autowired
  private var contentRepository: ContentRepository = _

  def findContentByName(contentName: String): ContentBase = {
    contentRepository.findBySchemaPropertyValue("name", contentName)
  }

  @Transactional(readOnly = true)
  def findContentById(id: Long): ContentBase = {
    contentRepository.findBySchemaPropertyValue("id", id)
  }

  @Transactional(readOnly = true)
  def getListOfAllContentPages(): List[ContentPage] = {
    val listOfContent: List[ContentPage] = contentRepository.getContentPages match {
      case null => Nil
      case Some(content) => content.toList
    }
    listOfContent
  }

//  private def convertNodesToContent(list: Path): List[ContentBase] = {
//    var convertList: ListBuffer[ContentBase] = ListBuffer()
//
//    for (node <- list.iterator().asScala) {
//      convertList += template.load(node, classOf[ContentBase])
//    }
//    convertList.result()
//  }

//  @Transactional(readOnly = true)
//  def getAllContentOfType[T](): List[ContentBase] = {
//
//    var contentIndex: Index[Node] = template.getIndex(classOf[T],"id")
//    var hits = contentIndex.getEntityType
//    //var hits = userCredentialIndex.query("userId", userId)
//
//    val listOfContent: List[ContentBase] = IteratorUtil.asCollection(contentRepository.findAll()).asScala.toList
//    listOfContent
//  }

  @Transactional(readOnly = false)
  def deleteContent(toDelete: ContentBase) {
    contentRepository.delete(toDelete)
  }

  @Transactional(readOnly = false)
  def deleteAllContent() {
    contentRepository.deleteAll()
  }


  @Transactional(readOnly = false)
  private def createContentPage(name: String, route: String = ""): ContentPage = {
    var newContent: ContentPage = null

    if (!route.isEmpty) {
      newContent = new ContentPage(name, route)
    } else {
      newContent = new ContentPage(name)
    }

    newContent = contentRepository.save(newContent)
    newContent
  }


}
