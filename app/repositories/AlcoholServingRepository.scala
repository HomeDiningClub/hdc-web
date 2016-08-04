package repositories

import java.util
import java.util.UUID

import models.event.{AlcoholServing}
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.repository.GraphRepository


trait AlcoholServingRepository extends GraphRepository[AlcoholServing] {

  // Auto-mapped by Spring
  @Query("MATCH (n:`AlcoholServing`{objectId:{0}}) RETURN n")
  def findByobjectId(objectId: String): AlcoholServing

  // Auto-mapped by Spring
  def findByName(name: String): util.List[AlcoholServing]

}
