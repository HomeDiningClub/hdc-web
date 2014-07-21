package repositories

import org.springframework.data.neo4j.repository.GraphRepository
import java.util.UUID
import models.location.County

trait CountyRepository extends GraphRepository[County] {

  // Auto-mapped by Spring
  def findByobjectId(objectId: UUID): County

}