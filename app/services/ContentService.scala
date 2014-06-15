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
    contentRepository.findOne(id)
  }

  @Transactional(readOnly = true)
  def getListOfAllContentPages(): List[ContentPage] = {
    val listOfContentPages: List[ContentPage] = template.findAll(classOf[ContentPage]).iterator.asScala.toList

//    val listOfContent: List[ContentPage] = contentRepository.getContentPages match {
//      case null => Nil
//      case content => content.toList
//    }
    listOfContentPages
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
  def deleteContentById(id: Long) {
    contentRepository.delete(id)
  }

  @Transactional(readOnly = false)
  def deleteAllContent() {
    contentRepository.deleteAll()
  }

  @Transactional(readOnly = false)
  def addContentPage(newContent: ContentPage): ContentPage = {
    val newContentResult = contentRepository.save(newContent)
    newContentResult
  }


}
