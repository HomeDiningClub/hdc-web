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
  def findByobjectId(objectId: UUID): RatesRecipe

  @Query("MATCH (userC:`UserCredential`)<-[ratings:`" + RelationshipTypesScala.RATED_RECIPE.Constant + "`]-(recipe:`Recipe`) WHERE userC.objectId={0} RETURN ratings")
  def findByuserWhoIsRating(userWhoIsRating: UUID): util.List[RatesRecipe]

  @Query("MATCH (userC:`UserCredential`)-[ratings:`" + RelationshipTypesScala.RATED_RECIPE.Constant + "`]->(recipe:`Recipe`) WHERE recipe.objectId={0} RETURN ratings")
  def findByuserRates(userRates: UUID): util.List[RatesRecipe]

  // filterModifier: "=", ">", "<"
  @Query("MATCH (ratings:`" + RelationshipTypesScala.RATED_RECIPE.Constant + "`) WHERE ratings.ratingValue {1} {0} RETURN ratings")
  def findByratingValue(ratingValue: Int, filterModifier: String): util.List[RatesRecipe]

  @Query("MATCH (userC:`UserCredential`)<-[ratings:`" + RelationshipTypesScala.RATED_RECIPE.Constant + "`]-(recipe:`Recipe`) WHERE userC.objectId={0} recipe.objectId={1} RETURN ratings")
    def findByuserWhoIsRatingAndUserRates(userWhoIsRating: UUID, userRates: UUID): util.List[RatesRecipe]
}
