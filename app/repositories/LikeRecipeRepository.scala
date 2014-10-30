package repositories

import models.like.UserCredentialLikeRecipe
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.repository.{RelationshipGraphRepository, GraphRepository}
import java.util.UUID
import java.util
import models.{Recipe, UserCredential}
import org.springframework.stereotype.Repository

trait LikeRecipeRepository extends GraphRepository[UserCredentialLikeRecipe] {

  @Query("MATCH (n:`UserCredentialLikeRecipe`) WHERE n.objectId={0} RETURN n")
  def findByobjectId(objectId: UUID): UserCredentialLikeRecipe

  @Query("MATCH (a)-[likes:`LIKES_RECIPE`]->(b) RETURN likes")
  def findAllLikes(): util.List[UserCredentialLikeRecipe]

  @Query("MATCH (a:`UserCredential`)-[likes:`LIKES_RECIPE`]->(b:`Recipe`) WHERE a.objectId = {0} RETURN likes")
  def findByuserWhoLikes(userWhoIsRating: UUID): util.List[UserCredentialLikeRecipe]

  @Query("MATCH (a:`UserCredential`)-[likes:`LIKES_RECIPE`]->(b:`Recipe`) WHERE b.objectId = {0} RETURN likes")
  def findByuserLikes(userLikes: UUID): util.List[UserCredentialLikeRecipe]

  // filterModifier: "=", ">", "<"
  @Query("MATCH (a)-[likes:`LIKES_RECIPE`]->(b) WHERE likes.ratingValue  {1} {0} RETURN likes")
  def findBylikes(likesValue: Boolean, filterModifier: String): util.List[UserCredentialLikeRecipe]

  @Query("MATCH (a:`UserCredential`)-[likes:`LIKES_RECIPE`]->(b:`Recipe`) WHERE a.objectId={0} AND b.objectId={1} RETURN likes")
  def findByuserWhoLikesAndUserLikes(userWhoIsLikes: String, userLikes: String): util.List[UserCredentialLikeRecipe]

}
