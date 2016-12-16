package repositories

import java.util
import java.util.UUID

import models.like.UserCredentialLikeEvent
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.repository.GraphRepository

trait LikeEventRepository extends GraphRepository[UserCredentialLikeEvent] {

  @Query("MATCH (n:`UserCredentialLikeEvent`) WHERE n.objectId={0} RETURN n")
  def findByobjectId(objectId: UUID): UserCredentialLikeEvent

  @Query("MATCH (a)-[likes:`LIKES_EVENT`]->(b) RETURN likes")
  def findAllLikes(): util.List[UserCredentialLikeEvent]

  @Query("MATCH (a:`UserCredential`)-[likes:`LIKES_EVENT`]->(b:`Event`) WHERE a.objectId = {0} RETURN likes")
  def findByuserWhoLikes(userWhoIsRating: UUID): util.List[UserCredentialLikeEvent]

  @Query("MATCH (a:`UserCredential`)-[likes:`LIKES_EVENT`]->(b:`Event`) WHERE b.objectId = {0} RETURN likes")
  def findByuserLikes(userLikes: UUID): util.List[UserCredentialLikeEvent]

  // filterModifier: "=", ">", "<"
  @Query("MATCH (a)-[likes:`LIKES_EVENT`]->(b) WHERE likes.ratingValue  {1} {0} RETURN likes")
  def findBylikes(likesValue: Boolean, filterModifier: String): util.List[UserCredentialLikeEvent]

  @Query("MATCH (a:`UserCredential`)-[likes:`LIKES_EVENT`]->(b:`Event`) WHERE a.objectId={0} AND b.objectId={1} RETURN likes")
  def findByuserWhoLikesAndUserLikes(userWhoIsLikes: String, userLikes: String): util.List[UserCredentialLikeEvent]

}
