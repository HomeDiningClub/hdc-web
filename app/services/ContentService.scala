package services

import javax.inject.{Named,Inject}

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.neo4j.support.Neo4jTemplate
import org.springframework.stereotype.Service
import models.content._
import traits.TransactionSupport
import scala.collection.JavaConverters._
import scala.List
import org.springframework.transaction.annotation.Transactional
import repositories._
import enums.{ContentStateEnums, ContentCategoryEnums}
import enums.ContentStateEnums.ContentStateEnums
import enums.ContentStateEnums
import java.util.UUID
import models.viewmodels.MenuItem
import controllers.routes
import scala.collection.immutable.HashSet
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

//@Named
//@Service
class ContentService @Inject()(val template: Neo4jTemplate,
                               val contentPageRepository: ContentPageRepository,
                               val relatedPageRepository: RelatedPageRepository) extends TransactionSupport {

  /*
  @Autowired
  private var template: Neo4jTemplate = _

  @Autowired
  private var contentPageRepository: ContentPageRepository = _

  @Autowired
  private var relatedPageRepository: RelatedPageRepository = _
  */


  def findContentPageByName(pageName: String): ContentPage = withTransaction(template){
    contentPageRepository.findBySchemaPropertyValue("name", pageName)
  }


  def findContentPageByRoute(routeName: String, contentState: ContentStateEnums = ContentStateEnums.PUBLISHED): Option[ContentPage] = withTransaction(template){
    contentPageRepository.findByRouteAndContentState(routeName,contentState.toString) match {
      case null => None
      case item => Some(item)
    }
  }



  def getAsideNewsItems: Option[List[ContentPage]] = withTransaction(template){
    contentPageRepository.findByContentCategoriesAndContentState(ContentCategoryEnums.ASIDE_STARTPAGE.toString,ContentStateEnums.PUBLISHED.toString).asScala.toList match {
      case null | Nil => None
      case items => Some(items.sortBy(i => i.name))
    }
  }

  def getNewsItems: Option[List[ContentPage]] = withTransaction(template){
    contentPageRepository.findByContentCategoriesAndContentState(ContentCategoryEnums.NEWS.toString,ContentStateEnums.PUBLISHED.toString).asScala.toList match {
      case null | Nil => None
      case items => Some(items.sortBy(i => i.name))
    }
  }


  def getMainMenuItems: Option[List[ContentPage]] = withTransaction(template){
    contentPageRepository.findByContentCategoriesAndVisibleInMenusAndContentState(ContentCategoryEnums.MAINMENU.toString,true,ContentStateEnums.PUBLISHED.toString).asScala.toList match {
      case null | Nil => None
      case items => Some(items.sortBy(i => i.name))
    }
  }


  def getQuickLinksItems: Option[List[ContentPage]] = withTransaction(template){
    contentPageRepository.findByContentCategoriesAndVisibleInMenusAndContentState(ContentCategoryEnums.QUICKLINKS.toString,true,ContentStateEnums.PUBLISHED.toString).asScala.toList match {
      case null | Nil => None
      case items => Some(items)
    }
  }


  def getTermsAndConditions: Option[ContentPage] = withTransaction(template){
    contentPageRepository.findByContentCategoriesAndContentState(ContentCategoryEnums.TERMS.toString,ContentStateEnums.PUBLISHED.toString).asScala.toList match {
      case null | Nil => None
      case items => Some(items.head)
    }
  }



  def getRelatedPages(objectId: UUID): Option[List[ContentPage]] = withTransaction(template){
    this.findContentById(objectId) match {
      case None => None
      case Some(cp) => cp.getRelatedPages.asScala.toList match {
        case null | Nil => None
        case items =>

          var list = items.sortBy(rp => rp.sortOrder).map {
            relPage: RelatedPage =>
              if(cp.objectId == relPage.relatedTo.objectId){
                relPage.relatedFrom
              }else{
                relPage.relatedTo
              }
          }.filter(p => p.isPublished && p.visibleInMenus).to[ListBuffer]

          // Add the current item as well
          list.prepend(cp)

          // Temporarily override sort order
          list = list.sortBy(contentPage => contentPage.name)

          Some(list.result())
      }
    }
  }

  def getAndMapRelatedPages(objectId: UUID): Option[List[MenuItem]] = {
    this.getRelatedPages(objectId) match {
      case None => None
      case Some(items) => Some(items.map {
        contentPage: ContentPage =>
          MenuItem(
            name = contentPage.name,
            title = contentPage.title,
            url = routes.ContentPageController.viewContentByName(contentPage.route).url,
            selected = if(contentPage.objectId == objectId){ true }else{ false }
          )
      })
    }
  }

  def mapRelatedPagesToStringOfObjectIds(item: ContentPage): List[String] = {
    item.getRelatedPages.asScala.toList.sortBy(rp => rp.sortOrder).map {
        relPage: RelatedPage =>
          if(item.objectId == relPage.relatedTo.objectId){
            relPage.relatedFrom.objectId.toString
          }else{
            relPage.relatedTo.objectId.toString
          }
      }
  }



  def getPagesAsDropDown(filterPage: Option[ContentPage] = None): Option[Seq[(String,String)]] = withTransaction(template){
    val returnItems: Option[Seq[(String,String)]] = this.getListOfAll match {
      case Some(items) =>
        val filteredItems = filterPage match {
          case None => items
          case Some(filterItem) => items.filter(p => p.objectId != filterItem.objectId)
        }

        var bufferList : mutable.Buffer[(String,String)] = mutable.Buffer[(String,String)]()

        // Map and add the rest
        filteredItems.sortBy(tw => tw.name).toBuffer.map {
          item: ContentPage =>
            bufferList += ((item.objectId.toString, item.name))
        }
        Some(bufferList.toSeq)
      case None =>
        None
    }
    returnItems
  }


  def getContentStatesAsDropDown: Seq[(String,String)] = {
    val returnItems: Seq[(String,String)] = Seq(
      (ContentStateEnums.PUBLISHED.toString,ContentStateEnums.PUBLISHED.toString),
      (ContentStateEnums.UNPUBLISHED.toString,ContentStateEnums.UNPUBLISHED.toString)
    )
    returnItems
  }

  def getCategoriesAsDropDown: Option[Seq[(String,String)]] = {
    val returnItems: Seq[(String,String)] = List(
      (ContentCategoryEnums.MAINMENU.toString,ContentCategoryEnums.MAINMENU.toString),
      (ContentCategoryEnums.NEWS.toString,ContentCategoryEnums.NEWS.toString),
      (ContentCategoryEnums.QUICKLINKS.toString,ContentCategoryEnums.QUICKLINKS.toString),
      (ContentCategoryEnums.ASIDE_STARTPAGE.toString,ContentCategoryEnums.ASIDE_STARTPAGE.toString),
      (ContentCategoryEnums.TERMS.toString,ContentCategoryEnums.TERMS.toString)
    )
    Some(returnItems)
  }





  def findContentById(objectId: java.util.UUID): Option[ContentPage] = withTransaction(template){
    val item = contentPageRepository.findByobjectId(objectId)

    item match {
      case null => None
      case page =>
//        if(fetchAll){
//          if(page.relatedPages != null)
//            template.fetch(page.relatedPages)
//        }
        Some(page)
    }
  }


  def getListOfAll: Option[List[ContentPage]] = withTransaction(template){
    val listOfAll: List[ContentPage] = contentPageRepository.findAll().iterator.asScala.toList

    if (listOfAll.isEmpty){
      None
    }else {

      // Lazy fetching
//      if(fetchAll){
//        val fetchedList = listOfAll.par.foreach { page =>
//          if(page.relatedPages != null)
//            template.fetch(page.relatedPages)
//        }
//        Some(fetchedList)
//      }
      Some(listOfAll)
    }
  }


  def removeAllRelatedPages(contentPage: ContentPage): Boolean = withTransaction(template){
    contentPage.removeAllRelatedPages
    true
  }


  def deleteContentPageById(objectId: java.util.UUID): Boolean = withTransaction(template){
    this.findContentById(objectId) match {
      case None => false
      case Some(item) =>
        contentPageRepository.delete(item)
        true
    }
  }


  def deleteAllContentPages() = withTransaction(template){
    contentPageRepository.deleteAll()
  }


  def addContentPage(newContent: ContentPage): ContentPage = withTransaction(template){
    val newContentResult = contentPageRepository.save(newContent)
    newContentResult
  }


}
