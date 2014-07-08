package services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.neo4j.support.Neo4jTemplate
import org.springframework.stereotype.Service
import models.content._
import scala.collection.JavaConverters._
import scala.List
import org.springframework.transaction.annotation.Transactional
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
    val contentPage: ContentPage = this.findContentById(id)
    if(contentPage != null)
    {
      contentPageRepository.delete(contentPage)
      return true
    }
    return false
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
