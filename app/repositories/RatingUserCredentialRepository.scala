package repositories

import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.repository.{AbstractGraphRepository, GraphRepository, RelationshipGraphRepository}
import java.util.UUID
import java.util

import models.rating.{RatesUserCredential, ReviewData}
import org.springframework.data.domain.{Page, Pageable}
import org.springframework.data.repository.query.Param

trait RatingUserCredentialRepository extends GraphRepository[RatesUserCredential] {

  @Query("MATCH (n:`RatesUserCredential`) WHERE n.objectId={0} RETURN n")
  def findByobjectId(objectId: UUID): RatesUserCredential

  @Query("MATCH (a)-[ratings:`RATED_USER`]->(b) RETURN ratings")
  def findAllRatings(): util.List[RatesUserCredential]

  @Query("MATCH (userC:`UserCredential`)-[rating:`RATED_USER`]->(ucIsRated:`UserCredential`) WHERE userC.objectId = {currentUserObjectId} AND ucIsRated.objectId = {hasRatedObjectId} RETURN rating")
  def hasRatedThisBefore(@Param("currentUserObjectId") currentUserObjectId: String, @Param("hasRatedObjectId") hasRatedObjectId: String): RatesUserCredential

  @Query("MATCH (upIsRating:`UserProfile`)<-[:IN_PROFILE]-(ucIsRating:`UserCredential`)-[rating:`RATED_USER`]->(ucIsRated:`UserCredential`)-[:IN_PROFILE]->(upIsRated:`UserProfile`) OPTIONAL MATCH (upIsRating)-[:`AVATAR_IMAGE`]-(userImage:`ContentFile`) RETURN rating.objectId as ReviewObjectId, rating.ratingValue as RatingValue, rating.ratingComment as ReviewText, rating.lastModifiedDate as LastModifiedDate, upIsRating.profileLinkName as UserWhoIsRatingProfileLinkName, ucIsRating.firstName as UserWhoIsRatingFirstName, upIsRated.profileLinkName as RatedProfileLinkName, upIsRated.profileLinkName as LinkToRatedItem, COLLECT(userImage.storeId) as UserWhoIsRatingAvatarImage ORDER BY rating.lastModifiedDate DESC")
  def findAllRatingsData(): util.List[ReviewData]

  @Query("MATCH (upIsRating:`UserProfile`)<-[:IN_PROFILE]-(ucIsRating:`UserCredential`)-[rating:`RATED_USER`]->(ucIsRated:`UserCredential`)-[:IN_PROFILE]->(upIsRated:`UserProfile`) OPTIONAL MATCH (upIsRating)-[:`AVATAR_IMAGE`]-(userImage:`ContentFile`) RETURN rating.objectId as ReviewObjectId, rating.ratingValue as RatingValue, rating.ratingComment as ReviewText, rating.lastModifiedDate as LastModifiedDate, upIsRating.profileLinkName as UserWhoIsRatingProfileLinkName, ucIsRating.firstName as UserWhoIsRatingFirstName, upIsRated.profileLinkName as RatedProfileLinkName, upIsRated.profileLinkName as LinkToRatedItem, COLLECT(userImage.storeId) as UserWhoIsRatingAvatarImage ORDER BY rating.lastModifiedDate DESC")
  def findAllRatingsDataPaged(pageable: Pageable) : Page[ReviewData]

  @Query("MATCH (a:`UserCredential`)-[ratings:`RATED_USER`]->(b:`UserCredential`) WHERE a.objectId = {0} RETURN ratings")
  def findByuserWhoIsRating(userWhoIsRating: UUID): util.List[RatesUserCredential]

  @Query("MATCH (upIsRating:`UserProfile`)<-[:IN_PROFILE]-(ucIsRating:`UserCredential`{objectId:{0}})-[rating:`RATED_USER`]->(ucIsRated:`UserCredential`)-[:IN_PROFILE]->(upIsRated:`UserProfile`) OPTIONAL MATCH (upIsRating)-[:`AVATAR_IMAGE`]-(userImage:`ContentFile`) RETURN rating.objectId as ReviewObjectId, rating.ratingValue as RatingValue, rating.ratingComment as ReviewText, rating.lastModifiedDate as LastModifiedDate, upIsRating.profileLinkName as UserWhoIsRatingProfileLinkName, ucIsRating.firstName as UserWhoIsRatingFirstName, upIsRated.profileLinkName as RatedProfileLinkName, upIsRated.profileLinkName as LinkToRatedItem, COLLECT(userImage.storeId) as UserWhoIsRatingAvatarImage ORDER BY rating.lastModifiedDate DESC")
  def findByuserWhoIsRatingData(userWhoIsRating: UUID): util.List[ReviewData]

  @Query("MATCH (a:`UserCredential`)-[ratings:`RATED_USER`]->(b:`UserCredential`) WHERE b.objectId = {0} RETURN ratings")
  def findByuserRates(userRates: UUID): util.List[RatesUserCredential]

  @Query("MATCH (upIsRating:`UserProfile`)<-[:IN_PROFILE]-(ucIsRating:`UserCredential`)-[rating:`RATED_USER`]->(ucIsRated:`UserCredential`{objectId:{0}})-[:IN_PROFILE]->(upIsRated:`UserProfile`) OPTIONAL MATCH (upIsRating)-[:`AVATAR_IMAGE`]-(userImage:`ContentFile`) RETURN rating.objectId as ReviewObjectId, rating.ratingValue as RatingValue, rating.ratingComment as ReviewText, rating.lastModifiedDate as LastModifiedDate, upIsRating.profileLinkName as UserWhoIsRatingProfileLinkName, ucIsRating.firstName as UserWhoIsRatingFirstName, upIsRated.profileLinkName as RatedProfileLinkName, upIsRated.profileLinkName as LinkToRatedItem, COLLECT(userImage.storeId) as UserWhoIsRatingAvatarImage ORDER BY rating.lastModifiedDate DESC")
  def findByuserRatesData(userRates: UUID): util.List[ReviewData]

  // filterModifier: "=", ">", "<"
  @Query("MATCH (a)-[ratings:`RATED_USER`]->(b) WHERE ratings.ratingValue {1} {0} RETURN ratings")
  def findByratingValue(ratingValue: Int, filterModifier: String): util.List[RatesUserCredential]

  @Query("MATCH (a:`UserCredential`)-[ratings:`RATED_USER`]->(b:`UserCredential`) WHERE a.objectId={0} AND b.objectId={1} RETURN ratings")
  def findByuserWhoIsRatingAndUserRates(userWhoIsRating: String, userRates: String): util.List[RatesUserCredential]

  @Query("MATCH ()-[r:`RATED_USER`]->() RETURN COUNT(r)")
  def getCountOfAll(): Int

  @Query("MATCH (uc:`UserCredential`)<-[r:`RATED_USER`]-(:`UserCredential`) WHERE uc.objectId={0} RETURN COUNT(r)")
  def getCountOfAllMemberRatingsForUser(userCredObjectId: String): Int


}
