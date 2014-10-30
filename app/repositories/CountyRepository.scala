package repositories

import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.repository.GraphRepository
import java.util.UUID
import models.location.County

trait CountyRepository extends GraphRepository[County] {

  // Auto-mapped by Spring
  @Query("MATCH (n:`County`) WHERE n.objectId={0} RETURN n")
  def findByobjectId(objectId: UUID): County

}