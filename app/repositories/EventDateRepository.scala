package repositories

import models.event.EventDate
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.repository.GraphRepository
import java.util.UUID
import java.util


trait EventDateRepository extends GraphRepository[EventDate] {

  // Auto-mapped by Spring
  @Query("MATCH (n:`EventDate`) WHERE n.objectId={0} RETURN n")
  def findByobjectId(objectId: UUID): EventDate

}
