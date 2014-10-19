package repositories


import org.springframework.data.neo4j.repository.{RelationshipGraphRepository, GraphRepository}
import java.util.UUID
import java.util
import models.rating.RatesUserCredential
import models.UserCredential

trait RatingUserCredentialRepository extends GraphRepository[RatesUserCredential] {

  // Auto-mapped by Spring
  def findByobjectId(objectId: UUID): RatesUserCredential
  def findByuserWhoIsRating(userWhoIsRating: UserCredential): util.List[RatesUserCredential]
  def findByuserRates(userRates: UserCredential): util.List[RatesUserCredential]
  def findByratingValue(ratingValue: Int): util.List[RatesUserCredential]
  def findByuserWhoIsRatingAndUserRates(userWhoIsRating: UserCredential, userRates: UserCredential): util.List[RatesUserCredential]
}
