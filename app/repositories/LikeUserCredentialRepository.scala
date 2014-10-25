package repositories

import models.like.UserCredentialLikeUserCredential
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.repository.{RelationshipGraphRepository, GraphRepository}
import java.util.UUID
import java.util
import models.UserCredential

trait LikeUserCredentialRepository extends GraphRepository[UserCredentialLikeUserCredential] {

  def findByobjectId(objectId: UUID): UserCredentialLikeUserCredential

  @Query("MATCH (a)-[likes:`LIKES_USER`]->(b) RETURN likes")
  def findAllLikes(): util.List[UserCredentialLikeUserCredential]

  @Query("MATCH (a:`UserCredential`)-[likes:`LIKES_USER`]->(b:`UserCredential`) WHERE a.objectId = {0} RETURN likes")
  def findByuserWhoLikes(userWhoIsRating: UUID): util.List[UserCredentialLikeUserCredential]

  @Query("MATCH (a:`UserCredential`)-[likes:`LIKES_USER`]->(b:`UserCredential`) WHERE b.objectId = {0} RETURN likes")
  def findByuserLikes(userRates: UUID): util.List[UserCredentialLikeUserCredential]

  // filterModifier: "=", ">", "<"
  @Query("MATCH (a)-[likes:`LIKES_USER`]->(b) WHERE likes.ratingValue  {1} {0} RETURN likes")
  def findBylikes(likesValue: Boolean, filterModifier: String): util.List[UserCredentialLikeUserCredential]

  @Query("MATCH (a:`UserCredential`)-[likes:`LIKES_USER`]->(b:`UserCredential`) WHERE a.objectId={0} AND b.objectId={1} RETURN likes")
  def findByuserWhoLikesAndUserLikes(userWhoIsLikes: String, userLikes: String): util.List[UserCredentialLikeUserCredential]

}
