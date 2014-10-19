package repositories

import models.like.UserCredentialLikeUserCredential
import org.springframework.data.neo4j.repository.{RelationshipGraphRepository, GraphRepository}
import java.util.UUID
import java.util
import models.UserCredential

trait LikeUserCredentialRepository extends GraphRepository[UserCredentialLikeUserCredential] {

  // Auto-mapped by Spring
  def findByobjectId(objectId: UUID): UserCredentialLikeUserCredential
  def findByuserWhoLikes(userWhoIsRating: UserCredential): util.List[UserCredentialLikeUserCredential]
  def findByuserLikes(userRates: UserCredential): util.List[UserCredentialLikeUserCredential]
  def findBylikes(likesValue: Boolean): util.List[UserCredentialLikeUserCredential]
  def findByuserWhoLikesAndUserLikes(userWhoIsLikes: UserCredential, userLikes: UserCredential): util.List[UserCredentialLikeUserCredential]

}
