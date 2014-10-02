package repositories

import org.springframework.data.neo4j.repository.GraphRepository
import java.util.UUID
import java.util
import models.rating.RatesRecipe
import models.{Recipe, UserCredential}

trait RatingRecipeRepository extends GraphRepository[RatesRecipe] {

  // Auto-mapped by Spring
  def findByobjectId(objectId: UUID): RatesRecipe
  def findByuserWhoIsRating(userWhoIsRating: UserCredential): util.List[RatesRecipe]
  def findByuserRates(userRates: Recipe): util.List[RatesRecipe]
  def findByratingValue(ratingValue: Int): util.List[RatesRecipe]
  def findByuserWhoIsRatingAndUserRates(userWhoIsRating: UserCredential, userRates: Recipe): util.List[RatesRecipe]
}
