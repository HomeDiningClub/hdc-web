package services

import models.files.ContentFile
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.{Page, Pageable, PageRequest}
import org.springframework.data.neo4j.support.Neo4jTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import scala.language.existentials
import repositories._
import models.{UserProfile, UserCredential, BlogPost}
import scala.collection.JavaConverters._
import scala.List
import java.util.UUID
import models.viewmodels.BlogPostsForm
import controllers.routes
import utils.Helpers
import models.viewmodels.BloggPostItem

import scala.collection.mutable.ListBuffer

@Service
class BloggPostsService {

  @Autowired
  private var template: Neo4jTemplate = _

  @Autowired
  private var bloggPostsRepository: BloggPostsRepository = _

  @Transactional(readOnly = true)
  def findById(objectId: UUID): Option[BlogPost] = {
    bloggPostsRepository.findByobjectId(objectId) match {
      case null => None
      case item => Some(item)
    }
  }

  @Transactional(readOnly = true)
  def getCountOfAll: Int = {
    bloggPostsRepository.getCountOfAll()
  }


  @Transactional(readOnly = true)
  def getListOfAll: List[BlogPost] = {
    bloggPostsRepository.findAll.iterator.asScala.toList match {
      case null => null
      case bloggPosts =>

        bloggPosts
    }
  }

  @Transactional(readOnly = false)
  def add(newContent: BlogPost): BlogPost = {
    val newContentResult = bloggPostsRepository.save(newContent)
    newContentResult
  }


  def stringToDate(dateString : String) : org.joda.time.DateTime = {
    var createdDateLong : Long = dateString.toLong
    new org.joda.time.DateTime(createdDateLong)
  }



  @Transactional(readOnly = true)
  def getBlogPostsBoxesPage(user: UserCredential, pageNo: Integer): Option[List[BloggPostItem]] = {

    val userObjectId = user.profiles.iterator().next().objectId.toString
    println("ObjectId : " + userObjectId)
    // var userObjectId2 = "3e6051cc-9dbd-4b4a-8148-81cc8797f74e"
    // val list = bloggPostsRepository.findAllUsersBloggPostsOnPage(userObjectId, new PageRequest(pageNo, 6))
    // findAllUsersBloggPost
    // val list = bloggPostsRepository.findAllUsersBloggPost(userObjectId)
    val list = bloggPostsRepository.findAllUsersBloggPostsOnPage(userObjectId, new PageRequest(pageNo, 6))



    val iterator = list.iterator()
    var antal : Int = 0
    var bloggPostList : ListBuffer[BloggPostItem] = new ListBuffer[BloggPostItem]

    while(iterator.hasNext()) {

      println("rad x...")
      antal = antal + 1

      val obj = iterator.next()


      // Image

      var mainImage = Some("/assets/images/profile/recipe-box-default-bw.png")
      if(obj.getMainImage().iterator().hasNext()){
        mainImage = Some(routes.ImageController.recipeBox(obj.getMainImage().iterator().next()).url)
      }


     // obj.getLastModDate(),
     // obj.getDateCreated(),




      // Build return-list
      var bloggPost = BloggPostItem(
        Some(UUID.fromString(obj.getBloggPostObjectId())),
        obj.getTitle(), obj.getText(), mainImage,
        list.hasNext,
        list.hasPrevious,
        list.getTotalPages,
        stringToDate(obj.getDateCreated()),
        stringToDate(obj.getLastModDate()),
        UUID.fromString(obj.getBloggPostObjectId()))

      bloggPostList += bloggPost

      println("prev : " + list.hasPrevious())
      println("next : " + list.hasNext())
      println("created date: " + obj.getDateCreated())
      println("mod date: " + obj.getLastModDate())

      // PUBLISHED
      println("mod date: " + obj.getState())
      println("getTotalElements : " + list.getTotalElements)

    }

    val blogPosts: List[BloggPostItem] = bloggPostList.toList

    println("antal = " + antal)

    if(blogPosts.isEmpty)
      None
    else
      Some(blogPosts)

  }


}
