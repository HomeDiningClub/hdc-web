package repositories

import org.springframework.data.neo4j.repository.GraphRepository
import models.World
import java.util.UUID


trait WorldRepository extends GraphRepository[World] {

  // Auto-mapped by Spring
  def findByobjectId(objectId: UUID): World

}