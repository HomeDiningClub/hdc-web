package services

import javax.inject.{Named,Inject}

import models.files.ContentFile
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.{Page, Pageable, PageRequest}
import org.springframework.data.neo4j.support.Neo4jTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import traits.TransactionSupport
import scala.language.existentials
import repositories._
import models.{UserProfile, UserCredential, BlogPost}
import scala.collection.JavaConverters._
import scala.List
import java.util.UUID
import controllers.routes
import customUtils.Helpers
import models.viewmodels.BlogPostItem

import scala.collection.mutable.ListBuffer
import models.formdata.BlogPostsForm

//@Named
//@Service
class BlogPostsService @Inject() (val template: Neo4jTemplate,
                                  val blogPostsRepository: BlogPostsRepository) extends TransactionSupport {


  def findById(objectId: UUID): Option[BlogPost] = withTransaction(template) {
    blogPostsRepository.findByobjectId(objectId.toString) match {
      case null => None
      case item => Some(item)
    }
  }

  def getCountOfAll: Int = withTransaction(template) {
    blogPostsRepository.getCountOfAll()
  }


  def getListOfAll: List[BlogPost] = withTransaction(template) {
    blogPostsRepository.findAll.iterator.asScala.toList match {
      case null => null
      case blogPosts =>

        blogPosts
    }
  }

  def add(newContent: BlogPost): BlogPost = withTransaction(template){
    val newContentResult = blogPostsRepository.save(newContent)
    newContentResult
  }


  def stringToDate(dateString : String) : org.joda.time.DateTime = {
    val createdDateLong : Long = dateString.toLong
    new org.joda.time.DateTime(createdDateLong)
  }



  //@Transactional(readOnly = true)
  def getBlogPostsBoxesPage(user: UserCredential, pageNo: Integer): Option[List[BlogPostItem]] = withTransaction(template){

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

  //@Transactional(readOnly = false)
  def deleteById(objectId: UUID): Boolean = withTransaction(template){
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
