package repositories

import models.like.UserCredentialLikeRecipe
import org.springframework.data.neo4j.repository.{RelationshipGraphRepository, GraphRepository}
import java.util.UUID
import java.util
import models.{Recipe, UserCredential}
import org.springframework.stereotype.Repository

@Repository
trait LikeRecipeRepository extends GraphRepository[UserCredentialLikeRecipe] {

  // Auto-mapped by Spring
  def findByobjectId(objectId: UUID): UserCredentialLikeRecipe
  def findByuserWhoLikes(userWhoIsRating: UserCredential): util.List[UserCredentialLikeRecipe]
  def findByuserLikes(userLikes: Recipe): util.List[UserCredentialLikeRecipe]
  def findBylikes(likesValue: Boolean): util.List[UserCredentialLikeRecipe]
  def findByuserWhoLikesAndUserLikes(userWhoIsLikes: UserCredential, userLikes: UserCredential): util.List[UserCredentialLikeRecipe]

}
