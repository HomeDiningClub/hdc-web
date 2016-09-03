package repositories

import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Page
import org.springframework.data.neo4j.annotation._
import org.springframework.data.neo4j.repository.GraphRepository
import models.{UserProfile, UserCredential, BlogPost}
import java.util.UUID
import java.util

trait BlogPostsRepository extends GraphRepository[BlogPost]
{

  // Auto-mapped by Spring
  @Query("MATCH (n:`BlogPost`) WHERE n.objectId={0} RETURN n")
  def findByobjectId(objectId: String): BlogPost

  @Query("MATCH (n:`BlogPost`) RETURN COUNT(*)")
  def getCountOfAll(): Int

  @Query("match (b:BlogPost)-[:HAS_BLOGPOSTS]-(p:UserProfile {objectId:{0}}) return b.title, b.text, b.objectId, b.lastModifiedDate, b.createdDate, b.contentState")
  def findAllUsersBlogPost(userObjectId: String) : util.List[BlogPostsData]

  @Query("match (b:BlogPost)-[:HAS_BLOGPOSTS]-(p:UserProfile {objectId:{0}}) optional match (b)-[:`MAIN_IMAGE`]-(mainImage:`ContentFile`) return b.title, b.text, b.objectId, b.lastModifiedDate, b.createdDate, b.contentState, COLLECT(mainImage.storeId) as MainImage order by b.createdDate desc")
  def findAllUsersBlogPostsOnPage(userObjectId: String, pageable: Pageable) : Page[BlogPostsData]

  // match (b:BlogPost)-[:HAS_BLOGPOSTS]-(p:UserProfile) optional match (b)-[:`MAIN_IMAGE`]-(mainImage:`ContentFile`)  return b.title, b.text, b.objectId, b.lastModifiedDate, b.createdDate, b.contentState, COLLECT(mainImage.storeId) as MainImage

  @QueryResult
  trait BlogPostsData {

    @ResultColumn("b.title")
    def getTitle() : String

    @ResultColumn("b.text")
    def getText() : String

    @ResultColumn("b.objectId")
    def getBlogPostObjectId() : String

    @ResultColumn("b.lastModifiedDate")
    def getLastModDate() : String

    @ResultColumn("b.contentState")
    def getState() : String

    @ResultColumn("b.createdDate")
    def getDateCreated() : String

    @ResultColumn("MainImage")
    def getMainImage() : util.List[String]
  }

}
