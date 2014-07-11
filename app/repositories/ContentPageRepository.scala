package repositories

import org.springframework.data.neo4j.repository.GraphRepository
import models.content._
import org.springframework.data.neo4j.annotation.Query
import java.util.UUID

trait ContentPageRepository extends GraphRepository[ContentPage] {

  // Auto-mapped by Spring
  def findByobjectId(objectId: UUID): ContentPage

}