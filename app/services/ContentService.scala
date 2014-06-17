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
import repositories._

@Service
class ContentService {

  @Autowired
  private var template: Neo4jTemplate = _

  @Autowired
  private var contentPageRepository: ContentPageRepository = _

  def findContentPageByName(pageName: String): ContentPage = {
    contentPageRepository.findBySchemaPropertyValue("name", pageName)
  }

  @Transactional(readOnly = true)
  def findContentById(id: Long): ContentPage = {
    contentPageRepository.findOne(id)
  }

  @Transactional(readOnly = true)
  def getListOfAllContentPages(): List[ContentPage] = {
    val listOfContentPages: List[ContentPage] = template.findAll(classOf[ContentPage]).iterator.asScala.toList
    listOfContentPages
  }

  @Transactional(readOnly = false)
  def deleteContentPageById(id: Long): Boolean = {
    if(this.findContentById(id) != null)
    {
      contentPageRepository.delete(id)
      true
    }
    false
  }

  @Transactional(readOnly = false)
  def deleteAllContentPages() {
    contentPageRepository.deleteAll()
  }

  @Transactional(readOnly = false)
  def addContentPage(newContent: ContentPage): ContentPage = {
    val newContentResult = contentPageRepository.save(newContent)
    newContentResult
  }


}
