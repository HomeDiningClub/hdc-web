package repositories

import models.modelconstants.RelationshipTypesScala
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.repository.GraphRepository
import java.util.UUID
import java.util
import models.rating.RatesRecipe
import models.{Recipe, UserCredential}

trait RatingRecipeRepository extends GraphRepository[RatesRecipe] {

  // Auto-mapped by Spring
  @Query("MATCH (n:`RatesRecipe`) WHERE n.objectId={0} RETURN n")
  def findByobjectId(objectId: UUID): RatesRecipe

  @Query("MATCH (a)-[ratings:`RATED_RECIPE`]->(b) RETURN ratings")
  def findAllRatings(): util.List[RatesRecipe]

  @Query("MATCH (userC:`UserCredential`)-[ratings:`RATED_RECIPE`]->(recipe:`Recipe`) WHERE userC.objectId = {0} RETURN ratings")
  def findByuserWhoIsRating(userWhoIsRating: UUID): util.List[RatesRecipe]

  @Query("MATCH (userC:`UserCredential`)-[ratings:`RATED_RECIPE`]->(recipe:`Recipe`) WHERE recipe.objectId = {0} RETURN ratings")
  def findByuserRates(userRates: UUID): util.List[RatesRecipe]

  @Query("MATCH (userC:`UserCredential`)-[ratings:`RATED_RECIPE`]->(recipe:`Recipe`)<-[:`HAS_RECIPES`]-(ownerP:`UserProfile`) WHERE ownerP.objectId = {0} RETURN ratings")
  def findRecipeRatingsByRecipeOwnerProfile(userRecipeOwnerProfile: UUID): util.List[RatesRecipe]

  // filterModifier: "=", ">", "<"
  @Query("MATCH (a)-[ratings:`RATED_RECIPE`]->(b) WHERE ratings.ratingValue  {1} {0} RETURN ratings")
  def findByratingValue(ratingValue: Int, filterModifier: String): util.List[RatesRecipe]

  @Query("MATCH (userC:`UserCredential`)<-[ratings:`RATED_RECIPE`]-(recipe:`Recipe`) WHERE userC.objectId={0} AND recipe.objectId={1} RETURN ratings")
  def findByuserWhoIsRatingAndUserRates(userWhoIsRating: String, userRates: String): util.List[RatesRecipe]
}
