package repositories

import org.springframework.data.neo4j.repository.GraphRepository
import models.{UserProfile, UserCredential, Recipe}
import java.util.UUID
import java.util

trait RecipeRepository extends GraphRepository[Recipe] {

  // Auto-mapped by Spring
  def findByobjectId(objectId: UUID): Recipe
  def findByrecipeLinkName(recipeLinkName: String): Recipe
  def findByownerProfileProfileLinkNameAndRecipeLinkName(profileLinkName: String, recipeLinkName: String): Recipe
  def findByownerProfile(ownerProfile: UserProfile): util.List[Recipe]
  def findByownerProfileObjectId(objectId: UUID): util.List[Recipe]
  def findByownerProfileOwner(owner: UserCredential): util.List[Recipe]
  def findByownerProfileOwnerObjectId(objectId: UUID): util.List[Recipe]
}