package repositories

import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.repository.{AbstractGraphRepository, RelationshipGraphRepository, GraphRepository}
import java.util.UUID
import java.util
import models.rating.RatesUserCredential

trait RatingUserCredentialRepository extends GraphRepository[RatesUserCredential] {

  // Auto-mapped by Spring
  @Query("MATCH (n:`RatesUserCredential`) WHERE n.objectId={0} RETURN n")
  def findByobjectId(objectId: UUID): RatesUserCredential

  @Query("MATCH (a)-[ratings:`RATED_USER`]->(b) RETURN ratings")
  def findAllRatings(): util.List[RatesUserCredential]

  @Query("MATCH (a:`UserCredential`)-[ratings:`RATED_USER`]->(b:`UserCredential`) WHERE a.objectId = {0} RETURN ratings")
  def findByuserWhoIsRating(userWhoIsRating: UUID): util.List[RatesUserCredential]

  @Query("MATCH (a:`UserCredential`)-[ratings:`RATED_USER`]->(b:`UserCredential`) WHERE b.objectId = {0} RETURN ratings")
  def findByuserRates(userRates: UUID): util.List[RatesUserCredential]

  // filterModifier: "=", ">", "<"
  @Query("MATCH (a)-[ratings:`RATED_USER`]->(b) WHERE ratings.ratingValue {1} {0} RETURN ratings")
  def findByratingValue(ratingValue: Int, filterModifier: String): util.List[RatesUserCredential]

  @Query("MATCH (a:`UserCredential`)-[ratings:`RATED_USER`]->(b:`UserCredential`) WHERE a.objectId={0} AND b.objectId={1} RETURN ratings")
  def findByuserWhoIsRatingAndUserRates(userWhoIsRating: String, userRates: String): util.List[RatesUserCredential]

  @Query("MATCH ()-[r:`RATED_USER`]->() RETURN COUNT(r)")
  def getCountOfAll(): Int

}
