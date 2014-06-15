package repositories

import org.springframework.data.neo4j.repository.GraphRepository
import models.content._
import org.springframework.data.neo4j.annotation.Query

trait ContentRepository extends GraphRepository[ContentBase]{

  @Query("MATCH (n:`ContentPage`) RETURN n")
  def getContentPages(): Option[Array[ContentPage]]

}