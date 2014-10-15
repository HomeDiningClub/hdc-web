package repositories

import java.util.UUID

import models.message.{Message}
import org.springframework.data.neo4j.repository.GraphRepository

/**
 * Created by Tommy on 15/10/2014.
 */
trait MessageRepository extends GraphRepository[Message]{

  // Auto-mapped by Spring
  def findByobjectId(objectId: UUID): Message
}
