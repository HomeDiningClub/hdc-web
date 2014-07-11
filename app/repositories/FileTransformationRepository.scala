package repositories

import org.springframework.data.neo4j.repository.GraphRepository
import models.files._
import play.Play
import fly.play.s3.S3
import org.springframework.data.annotation.Transient
import java.util.UUID


trait FileTransformationRepository extends GraphRepository[FileTransformation] {

  // Auto-mapped by Spring
  def findByobjectId(objectId: UUID): FileTransformation

  // Auto-mapped by Spring
  def findByName(name: String): FileTransformation

}
