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

  @Query("MATCH (n:`Recipe`) RETURN COUNT(*)")
  def getCountOfAll(): Int

  @Query("match (tag {objectId:{0}})-[:IN_PROFILE]->(uc:UserProfile)-[:HAS_RECIPES]-(r:Recipe) optional match (tag)-[:IN_PROFILE]->(uc:UserProfile)-[:HAS_RECIPES]-(r:Recipe) optional match (r)-[:IMAGES]-(recipeImages:`ContentFile`) optional match (r)-[g]-(ux:UserCredential) optional match (r)-[:`MAIN_IMAGE`]-(mainImage:`ContentFile`) return avg(g.ratingValue), r.name, r.preAmble, r.mainBody, r.objectId, COLLECT(recipeImages.storeId) as RecipeImages, COLLECT(mainImage.storeId) as MainImage, uc.profileLinkName, r.recipeLinkName, tag.userId")
  def findRecipes(userObjectId: String) : util.List[RecipeData]

  @QueryResult
  trait RecipeData {

    @ResultColumn("uc.profileLinkName")
    def getprofileLinkName() : String

    @ResultColumn("r.recipeLinkName")
    def getLinkName() : String

    @ResultColumn("r.name")
    def getName() : String

    @ResultColumn("r.objectId")
    def getobjectId() : String

    @ResultColumn("r.preAmble")
    def getpreAmble() : String

    @ResultColumn("r.mainBody")
    def getMainBody() : String

    @ResultColumn("avg(g.ratingValue)")
    def getRating() : String

    @ResultColumn("RecipeImages")
    def getRecipeImage() : util.List[String]

    @ResultColumn("MainImage")
    def getMainImage() : util.List[String]

    @ResultColumn("tag.userId")
    def getUserId() : String

  }

  @Query("match (tag {objectId:{0}})-[:IN_PROFILE]->(uc:UserProfile)-[:HAS_RECIPES]-(r:Recipe) optional match (tag)-[:IN_PROFILE]->(uc:UserProfile)-[:HAS_RECIPES]-(r:Recipe) optional match (r)-[:IMAGES]-(recipeImages:`ContentFile`) optional match (r)-[g]-(ux:UserCredential) optional match (r)-[:`MAIN_IMAGE`]-(mainImage:`ContentFile`) return avg(g.ratingValue), r.name, r.preAmble, r.mainBody, r.objectId, COLLECT(recipeImages.storeId) as RecipeImages, COLLECT(mainImage.storeId) as MainImage, uc.profileLinkName, r.recipeLinkName, tag.userId")
  def findRecipesOnPage(userObjectId: String, pageable: Pageable) : Page[RecipeData]

  def findByrecipeLinkName(recipeLinkName: String): Recipe
  def findByownerProfileProfileLinkNameAndRecipeLinkName(profileLinkName: String, recipeLinkName: String): Recipe
  def findByownerProfile(ownerProfile: UserProfile): util.List[Recipe]
  def findByownerProfileObjectId(objectId: UUID): util.List[Recipe]
  def findByownerProfileOwner(owner: UserCredential): util.List[Recipe]
  def findByownerProfileOwnerObjectId(objectId: UUID): util.List[Recipe]
}