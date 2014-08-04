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
  def findContentById(objectId: java.util.UUID, fetchAll: Boolean = false): ContentPage = {
    val item = contentPageRepository.findByobjectId(objectId)

    item match {
      case null => null
      case page =>
        if(fetchAll){
          if(page.parentPage != null)
            template.fetch(page.parentPage)
        }
        page
    }
  }

  @Transactional(readOnly = true)
  def getListOfAll(fetchAll: Boolean = false): Option[List[ContentPage]] = {
    val listOfAll: List[ContentPage] = contentPageRepository.findAll().iterator.asScala.toList

    if (listOfAll.isEmpty){
      None
    }else {

      // Lazy fetching
      if(fetchAll){
        val fetchedList = listOfAll.par.foreach { p =>
          if(p.parentPage != null)
            template.fetch(p.parentPage)
        }
        Some(fetchedList)
      }
      Some(listOfAll)
    }
  }

  @Transactional(readOnly = false)
  def deleteContentPageById(objectId: java.util.UUID): Boolean = {
    val contentPage: ContentPage = this.findContentById(objectId)
    if(contentPage != null)
    {
      contentPageRepository.delete(contentPage)
      return true
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
