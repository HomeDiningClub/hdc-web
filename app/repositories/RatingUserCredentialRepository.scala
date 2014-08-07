package repositories

import models.profile.TagWord
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.repository.GraphRepository
import java.util.UUID
import java.util
import models.rating.RatingUserCredential
import models.UserCredential


trait RatingUserCredentialRepository extends GraphRepository[RatingUserCredential] {

  // Auto-mapped by Spring
  def findByobjectId(objectId: UUID): RatingUserCredential
  def findByuserWhoIsRating(userWhoIsRating: UserCredential): util.List[RatingUserCredential]
  def findByuserRates(userRates: UserCredential): util.List[RatingUserCredential]
  def findByratingValue(ratingValue: Int): util.List[RatingUserCredential]

}
