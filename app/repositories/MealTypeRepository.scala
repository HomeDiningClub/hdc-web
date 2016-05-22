package repositories

import models.event.MealType
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.repository.GraphRepository
import java.util.UUID
import java.util


trait MealTypeRepository extends GraphRepository[MealType] {

  // Auto-mapped by Spring
  @Query("MATCH (n:`MealType`{objectId:{0}}) RETURN n")
  def findByobjectId(objectId: String): MealType

  // Auto-mapped by Spring
  def findByName(name: String): util.List[MealType]

}
