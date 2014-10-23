package repositories


import models.modelconstants.RelationshipTypesScala
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.repository.GraphRepository
import java.util.UUID
import java.util
import models.rating.RatesUserCredential
import models.UserCredential

trait RatingUserCredentialRepository extends GraphRepository[RatesUserCredential] {

  // Auto-mapped by Spring
  def findByobjectId(objectId: UUID): RatesUserCredential

  @Query("MATCH (userC:`UserCredential`)<-[ratings:`" + RelationshipTypesScala.RATED_USER.Constant + "`]-(userCRated:`UserCredential`) WHERE userC.objectId={0} RETURN ratings")
  def findByuserWhoIsRating(userWhoIsRating: UUID): util.List[RatesUserCredential]

  @Query("MATCH (userC:`UserCredential`)-[ratings:`" + RelationshipTypesScala.RATED_USER.Constant + "`]->(userCRated:`UserCredential`) WHERE userC.objectId={0} RETURN ratings")
  def findByuserRates(userRates: UUID): util.List[RatesUserCredential]

  // filterModifier: "=", ">", "<"
  @Query("MATCH (ratings:`" + RelationshipTypesScala.RATED_USER.Constant + "`) WHERE ratings.ratingValue {1} {0} RETURN ratings")
  def findByratingValue(ratingValue: Int, filterModifier: String): util.List[RatesUserCredential]

  @Query("MATCH (userC:`UserCredential`)<-[ratings:`" + RelationshipTypesScala.RATED_USER.Constant + "`]-(userCRated:`UserCredential`) WHERE userC.objectId={0} userCRated.objectId={1} RETURN ratings")
  def findByuserWhoIsRatingAndUserRates(userWhoIsRating: UUID, userRates: UUID): util.List[RatesUserCredential]
}
