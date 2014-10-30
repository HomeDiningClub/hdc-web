package repositories

import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.repository.GraphRepository
import models.World
import java.util.UUID


trait WorldRepository extends GraphRepository[World] {

  // Auto-mapped by Spring
  @Query("MATCH (n:`World`) WHERE n.objectId={0} RETURN n")
  def findByobjectId(objectId: UUID): World

}