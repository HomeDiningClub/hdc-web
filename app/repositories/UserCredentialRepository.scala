package repositories

import models.UserCredential
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.repository.GraphRepository
import java.util.UUID
import java.util

trait UserCredentialRepository extends GraphRepository[UserCredential] {

  // Auto-mapped by Spring
  @Query("MATCH (n:`UserCredential`) WHERE n.objectId={0} RETURN n")
  def findByobjectId(objectId: UUID): UserCredential

  //def findByuserIdAndproviderId(userId : String, providerId: String) : UserCredential
  @Query("MATCH (n:`UserCredential`) WHERE n.userId={0} AND n.providerId={1} RETURN n")
  def findByuserIdAndProviderId(userId : String, providerId: String) : UserCredential

  @Query("MATCH (n:`UserCredential`) WHERE n.emailAddress=lower({0}) AND n.providerId={1} RETURN n")
  def findByemailAddressAndProviderIdToLower(emailAddress : String, providerId: String) : UserCredential

  def findByemailAddressAndProviderId(emailAddress : String, providerId: String) : UserCredential
  def findByuserId(userId : String) : UserCredential
  def findByuserId(userId : String, providerId: String) : UserCredential

  // Rating Users
  def findByratingsUserWhoIsRating(userWhoIsRating: UserCredential): util.List[UserCredential]
  def findByratingsUserRates(userRates: UserCredential): util.List[UserCredential]
  def findByratingsRatingValue(ratingValue: Int): util.List[UserCredential]

  @Query("start up=node:UserCredential(email={0}) return up")
  def getUserCredentials(email: String ): Array[UserCredential]

  @Query("MATCH (n:`UserCredential`) RETURN COUNT(*)")
  def getCountOfAll(): Int

}
