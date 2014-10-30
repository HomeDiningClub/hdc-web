package repositories

import java.util.UUID

import models.message.{Message}
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.repository.GraphRepository

/**
 * Created by Tommy on 15/10/2014.
 */
trait MessageRepository extends GraphRepository[Message]{

  @Query("MATCH (n:`Message`) WHERE n.objectId={0} RETURN n")
  def findByobjectId(objectId: UUID): Message
}
