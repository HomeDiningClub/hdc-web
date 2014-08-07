package repositories

import org.springframework.data.neo4j.repository.GraphRepository
import models.UserCredential
import org.springframework.data.neo4j.annotation.Query
import securesocial.core.{IdentityId, Identity}
import org.springframework.data.neo4j.repository.GraphRepository
import java.util.UUID
import java.util

trait UserCredentialRepository extends GraphRepository[UserCredential]
{

  // Auto-mapped by Spring
  def findByobjectId(objectId: UUID): UserCredential
  //def findByuserIdAndproviderId(userId : String, providerId: String) : UserCredential
  def findByuserIdAndProviderId(userId : String, providerId: String) : UserCredential
  def findByemailAddressAndProviderId(emailAddress : String, providerId: String) : UserCredential
  def findByuserId(userId : String) : UserCredential
  def findByuserId(userId : String, providerId: String) : UserCredential

  // Rating
  def findByratingsUserWhoIsRating(userWhoIsRating: UserCredential): util.List[UserCredential]
  def findByratingsUserRates(userRates: UserCredential): util.List[UserCredential]
  def findByratingsRatingValue(ratingValue: Int): util.List[UserCredential]

  @Query("start up=node:UserCredential(email={0}) return up")
  def getUserCredentials(email: String ): Array[UserCredential]

  @Query("start up=node:UserCredential(identity={0}) return up")
  def getUserCredential(identityId: IdentityId ): UserCredential

}
