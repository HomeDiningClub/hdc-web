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
import models.viewmodels.BlogPostItem

import scala.collection.mutable.ListBuffer

@Service
class BlogPostsService {

  @Autowired
  private var template: Neo4jTemplate = _

  @Autowired
  private var blogPostsRepository: BlogPostsRepository = _

  @Transactional(readOnly = true)
  def findById(objectId: UUID): Option[BlogPost] = {
    blogPostsRepository.findByobjectId(objectId) match {
      case null => None
      case item => Some(item)
    }
  }

  @Transactional(readOnly = true)
  def getCountOfAll: Int = {
    blogPostsRepository.getCountOfAll()
  }


  @Transactional(readOnly = true)
  def getListOfAll: List[BlogPost] = {
    blogPostsRepository.findAll.iterator.asScala.toList match {
      case null => null
      case blogPosts =>

        blogPosts
    }
  }

  @Transactional(readOnly = false)
  def add(newContent: BlogPost): BlogPost = {
    val newContentResult = blogPostsRepository.save(newContent)
    newContentResult
  }


  def stringToDate(dateString : String) : org.joda.time.DateTime = {
    var createdDateLong : Long = dateString.toLong
    new org.joda.time.DateTime(createdDateLong)
  }



  @Transactional(readOnly = true)
  def getBlogPostsBoxesPage(user: UserCredential, pageNo: Integer): Option[List[BlogPostItem]] = {

    val userObjectId = user.profiles.iterator().next().objectId.toString
    println("ObjectId : " + userObjectId)
    // var userObjectId2 = "3e6051cc-9dbd-4b4a-8148-81cc8797f74e"
    // val list = blogPostsRepository.findAllUsersBlogPostsOnPage(userObjectId, new PageRequest(pageNo, 6))
    // findAllUsersBlogPost
    // val list = blogPostsRepository.findAllUsersBlogPost(userObjectId)
    val list = blogPostsRepository.findAllUsersBlogPostsOnPage(userObjectId, new PageRequest(pageNo, 6))



    val iterator = list.iterator()
    var antal : Int = 0
    var blogPostList : ListBuffer[BlogPostItem] = new ListBuffer[BlogPostItem]

    while(iterator.hasNext()) {

      println("rad x...")
      antal = antal + 1

      val obj = iterator.next()


      // Image

      var mainImage: Option[String] = None
      if(obj.getMainImage().iterator().hasNext()){
        mainImage = Some(routes.ImageController.blogNormal(obj.getMainImage().iterator().next()).url)
      }


     // obj.getLastModDate(),
     // obj.getDateCreated(),




      // Build return-list
      var blogPost = BlogPostItem(
        Some(UUID.fromString(obj.getBlogPostObjectId())),
        obj.getTitle(), obj.getText(), mainImage,
        list.hasNext,
        list.hasPrevious,
        list.getTotalPages,
        stringToDate(obj.getDateCreated()),
        stringToDate(obj.getLastModDate()),
        UUID.fromString(obj.getBlogPostObjectId()))

      blogPostList += blogPost

      println("prev : " + list.hasPrevious())
      println("next : " + list.hasNext())
      println("created date: " + obj.getDateCreated())
      println("mod date: " + obj.getLastModDate())

      // PUBLISHED
      println("mod date: " + obj.getState())
      println("getTotalElements : " + list.getTotalElements)

    }

    val blogPosts: List[BlogPostItem] = blogPostList.toList

    println("antal = " + antal)

    if(blogPosts.isEmpty)
      None
    else
      Some(blogPosts)

  }

  @Transactional(readOnly = false)
  def deleteById(objectId: UUID): Boolean = {
    this.findById(objectId) match {
      case None => false
      case Some(item) =>
        item.deleteMainImage()
        //item.deleteRatings()
        //item.deleteLikes()
        blogPostsRepository.delete(item)
        true
    }
  }


}