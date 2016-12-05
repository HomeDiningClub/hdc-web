package repositories

import models.modelconstants.RelationshipTypesScala
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.repository.GraphRepository
import java.util.UUID
import java.util

import models.rating.{RatesRecipe, ReviewData}
import models.{Recipe, UserCredential}
import org.springframework.data.repository.query.Param

trait RatingRecipeRepository extends GraphRepository[RatesRecipe] {

  @Query("MATCH (n:`RatesRecipe`) WHERE n.objectId={0} RETURN n")
  def findByobjectId(objectId: UUID): RatesRecipe

  @Query("MATCH (a)-[ratings:`RATED_RECIPE`]->(b) RETURN ratings")
  def findAllRatings(): util.List[RatesRecipe]

  @Query("MATCH (userC:`UserCredential`)-[rating:`RATED_RECIPE`]->(recipe:`Recipe`) WHERE userC.objectId = {currentUserObjectId} AND recipe.objectId = {hasRatedObjectId} RETURN rating")
  def hasRatedThisBefore(@Param("currentUserObjectId") currentUserObjectId: String, @Param("hasRatedObjectId") hasRatedObjectId: String): RatesRecipe

  @Query("MATCH (userC:`UserCredential`)-[ratings:`RATED_RECIPE`]->(recipe:`Recipe`) WHERE userC.objectId = {0} RETURN ratings")
  def findByuserWhoIsRating(userWhoIsRating: UUID): util.List[RatesRecipe]

  @Query("MATCH (upIsRating:`UserProfile`)<-[:IN_PROFILE]-(ucIsRating:`UserCredential`{objectId:{0}})-[rating:`RATED_RECIPE`]->(recipe:`Recipe`)<-[:`HAS_RECIPES`]-(upIsRated:`UserProfile`)<-[:IN_PROFILE]-(ucIsRated:`UserCredential`) OPTIONAL MATCH (upIsRating)-[:`AVATAR_IMAGE`]-(userImage:`ContentFile`) RETURN rating.objectId as ReviewObjectId, rating.ratingValue as RatingValue, rating.ratingComment as ReviewText, rating.lastModifiedDate as LastModifiedDate, upIsRating.profileLinkName as UserWhoIsRatingProfileLinkName, ucIsRating.firstName as UserWhoIsRatingFirstName, upIsRated.profileLinkName as RatedProfileLinkName, recipe.recipeLinkName as LinkToRatedItem, recipe.name as NameOfRatedItem, COLLECT(userImage.storeId) as UserWhoIsRatingAvatarImage ORDER BY rating.lastModifiedDate DESC")
  def findByuserWhoIsRatingData(userWhoIsRating: UUID): util.List[ReviewData]

  @Query("MATCH (userC:`UserCredential`)-[ratings:`RATED_RECIPE`]->(recipe:`Recipe`) WHERE recipe.objectId = {0} RETURN ratings")
  def findByuserRates(userRates: UUID): util.List[RatesRecipe]

  @Query("MATCH (userC:`UserCredential`)-[ratings:`RATED_RECIPE`]->(recipe:`Recipe`)<-[:`HAS_RECIPES`]-(ownerP:`UserProfile`) WHERE ownerP.objectId = {0} RETURN ratings")
  def findRecipeRatingsByRecipeOwnerProfile(userRecipeOwnerProfile: UUID): util.List[RatesRecipe]


  @Query("MATCH (upIsRating:`UserProfile`)<-[:IN_PROFILE]-(ucIsRating:`UserCredential`)-[rating:`RATED_RECIPE`]->(recipe:`Recipe`)<-[:`HAS_RECIPES`]-(upIsRated:`UserProfile`{objectId:{0}})<-[:IN_PROFILE]-(ucIsRated:`UserCredential`) OPTIONAL MATCH (upIsRating)-[:`AVATAR_IMAGE`]-(userImage:`ContentFile`) RETURN rating.objectId as ReviewObjectId, rating.ratingValue as RatingValue, rating.ratingComment as ReviewText, rating.lastModifiedDate as LastModifiedDate, upIsRating.profileLinkName as UserWhoIsRatingProfileLinkName, ucIsRating.firstName as UserWhoIsRatingFirstName, upIsRated.profileLinkName as RatedProfileLinkName, recipe.recipeLinkName as LinkToRatedItem, recipe.name as NameOfRatedItem, COLLECT(userImage.storeId) as UserWhoIsRatingAvatarImage ORDER BY rating.lastModifiedDate DESC")
  def findRecipeRatingsByRecipeOwnerProfileData(userRecipeOwnerProfile: UUID): util.List[ReviewData]

  // filterModifier: "=", ">", "<"
  @Query("MATCH (a)-[ratings:`RATED_RECIPE`]->(b) WHERE ratings.ratingValue  {1} {0} RETURN ratings")
  def findByratingValue(ratingValue: Int, filterModifier: String): util.List[RatesRecipe]

  @Query("MATCH (userC:`UserCredential`)<-[ratings:`RATED_RECIPE`]-(recipe:`Recipe`) WHERE userC.objectId={0} AND recipe.objectId={1} RETURN ratings")
  def findByuserWhoIsRatingAndUserRates(userWhoIsRating: String, userRates: String): util.List[RatesRecipe]

  @Query("MATCH ()-[r:`RATED_RECIPE`]->() RETURN COUNT(r)")
  def getCountOfAll(): Int

}
