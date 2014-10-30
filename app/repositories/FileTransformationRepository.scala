package repositories

import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.repository.GraphRepository
import models.files._
import play.Play
import fly.play.s3.S3
import org.springframework.data.annotation.Transient
import java.util.UUID


trait FileTransformationRepository extends GraphRepository[FileTransformation] {

  @Query("MATCH (n:`FileTransformation`) WHERE n.objectId={0} RETURN n")
  def findByobjectId(objectId: UUID): FileTransformation

  @Query("MATCH (n:`FileTransformation`) WHERE n.name={0} RETURN n")
  def findByName(name: String): FileTransformation

}
