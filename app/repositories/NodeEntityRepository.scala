package repositories

import org.neo4j.graphdb.Node
import org.springframework.data.neo4j.repository.GraphRepository
import java.util.UUID
import org.springframework.data.neo4j.annotation.{NodeEntity, Query}
import models.entity.EmptyNode

trait NodeEntityRepository extends GraphRepository[EmptyNode] {

  //@Query("START n = node(*) MATCH n WHERE n.objectId='{0}' return n")
  @Query("START n = node(*) MATCH n WHERE n.objectId={0} return n")
  def getAnyNodeUsingId(objectId: UUID): EmptyNode

}
