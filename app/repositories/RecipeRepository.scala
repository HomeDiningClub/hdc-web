package repositories

import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Page
import org.springframework.data.neo4j.annotation.{QueryResult, Query, ResultColumn, MapResult}
import org.springframework.data.neo4j.repository.GraphRepository
import models.{UserProfile, UserCredential, Recipe}
import java.util.UUID
import java.util

trait RecipeRepository extends GraphRepository[Recipe] {

  // Auto-mapped by Spring
  @Query("MATCH (n:`Recipe`) WHERE n.objectId={0} RETURN n")
  def findByobjectId(objectId: UUID): Recipe

  /*
  @Query("match tag-[:IN_PROFILE]->(uc:UserProfile)-[:HAS_RECIPES]-(r:Recipe)" +
    " where tag.emailAddress={0}" +
    " return r.recipeLinkName, r.name, r.objectId, r.preAmble"
  )
  */
 /*
  @Query("match tag-[:IN_PROFILE]->(uc:UserProfile)-[:HAS_RECIPES]-(r:Recipe)" +
  " optional match tag-[:IN_PROFILE]->(uc:UserProfile)-[:HAS_RECIPES]-(r:Recipe)-[f]-(ux:UserCredential)" +
  " where tag.emailAddress={0}" +
  " return avg(f.ratingValue), r.name, r.preAmble, r.objectId, r.recipeLinkName, uc.profileLinkName"
  )*/

  @Query(
  "match (tag {objectId:{0}})-[:IN_PROFILE]->(uc:UserProfile)-[:HAS_RECIPES]-(r:Recipe)" +
  " optional match (tag)-[:IN_PROFILE]->(uc:UserProfile)-[:HAS_RECIPES]-(r:Recipe)" +
  " optional match (r)-[:IMAGES]-(recipeImages:`ContentFile`)" +
  " optional match (r)-[g]-(ux:UserCredential)" +
  " optional match (r)-[:`MAIN_IMAGE`]-(mainImage:`ContentFile`)" +
  " return avg(g.ratingValue), r.name, r.preAmble, r.mainBody, r.objectId," +
  " COLLECT(recipeImages.storeId) as RecipeImages," +
  " COLLECT(mainImage.storeId) as MainImage, uc.profileLinkName, r.recipeLinkName, tag.userId"
  )
  def findRecipes(userObjectId: String) : util.List[RecipeData]

  //@MapResult
  @QueryResult
  trait RecipeData {

    // uc.profileLinkName
    @ResultColumn("uc.profileLinkName")
    def getprofileLinkName() : String

    // r.recipeLinkName
    @ResultColumn("r.recipeLinkName")
    def getLinkName() : String

    // r.name
    @ResultColumn("r.name")
    def getName() : String

    // r.objectId
    @ResultColumn("r.objectId")
    def getobjectId() : String

    // r.preAmble
    @ResultColumn("r.preAmble")
    def getpreAmble() : String

    // r.mainBody
    @ResultColumn("r.mainBody")
    def getMainBody() : String

    // avg(g.ratingValue)
    @ResultColumn("avg(g.ratingValue)")
    def getRating() : String

    // COLLECT(recipeImages.storeId)
    @ResultColumn("RecipeImages")
    def getRecipeImage() : util.List[String]

    // COLLECT(mainImage.storeId)
    @ResultColumn("MainImage")
    def getMainImage() : util.List[String]

    // tag.userId
    @ResultColumn("tag.userId")
    def getUserId() : String

  }

  @Query(
    "match (tag {objectId:{0}})-[:IN_PROFILE]->(uc:UserProfile)-[:HAS_RECIPES]-(r:Recipe)" +
      " optional match (tag)-[:IN_PROFILE]->(uc:UserProfile)-[:HAS_RECIPES]-(r:Recipe)" +
      " optional match (r)-[:IMAGES]-(recipeImages:`ContentFile`)" +
      " optional match (r)-[g]-(ux:UserCredential)" +
      " optional match (r)-[:`MAIN_IMAGE`]-(mainImage:`ContentFile`)" +
      " return avg(g.ratingValue), r.name, r.preAmble, r.mainBody, r.objectId," +
      " COLLECT(recipeImages.storeId) as RecipeImages," +
      " COLLECT(mainImage.storeId) as MainImage, uc.profileLinkName, r.recipeLinkName, tag.userId"
  )
  def findRecipesOnPage(userObjectId: String, pageable : Pageable) : Page[RecipeData]

  def findByrecipeLinkName(recipeLinkName: String): Recipe
  def findByownerProfileProfileLinkNameAndRecipeLinkName(profileLinkName: String, recipeLinkName: String): Recipe
  def findByownerProfile(ownerProfile: UserProfile): util.List[Recipe]
  def findByownerProfileObjectId(objectId: UUID): util.List[Recipe]
  def findByownerProfileOwner(owner: UserCredential): util.List[Recipe]
  def findByownerProfileOwnerObjectId(objectId: UUID): util.List[Recipe]
}