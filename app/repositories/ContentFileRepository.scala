package repositories

import org.springframework.data.neo4j.repository.GraphRepository
import models.files._
import java.util
import java.util.UUID


trait ContentFileRepository extends GraphRepository[ContentFile] {

  // Auto-mapped by Spring
  def findByobjectId(objectId: UUID): ContentFile

  // Auto-mapped by Spring
  def findBybaseContentType(name: String): util.List[ContentFile]

}
