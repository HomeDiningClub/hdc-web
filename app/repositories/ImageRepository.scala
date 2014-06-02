package repositories

import org.springframework.data.neo4j.repository.GraphRepository
import models.files._
import play.Play
import fly.play.s3.S3
import org.springframework.data.annotation.Transient


trait ImageRepository extends GraphRepository[ImageFile] {

}
