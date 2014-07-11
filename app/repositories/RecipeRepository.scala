package repositories

import org.springframework.data.neo4j.repository.GraphRepository
import models.content._
import org.springframework.data.neo4j.annotation.Query
import models.Recipe
import java.util.UUID

trait RecipeRepository extends GraphRepository[Recipe] {

  // Auto-mapped by Spring
  def findByobjectId(objectId: UUID): Recipe

}