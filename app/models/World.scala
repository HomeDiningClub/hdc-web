package models

import java.util.Set
import org.neo4j.graphdb.Direction
import org.neo4j.graphdb.RelationshipType
import org.springframework.data.neo4j.annotation.Fetch
import org.springframework.data.neo4j.annotation.GraphId
import org.springframework.data.neo4j.annotation.Indexed
import org.springframework.data.neo4j.annotation.NodeEntity
import org.springframework.data.neo4j.annotation.RelatedTo

@NodeEntity
class World(@Indexed var name: String,@Indexed(indexName = "moon-index") var moons: Int) {
  @GraphId
  var id: java.lang.Long = _

  // TODO Polish on enums
  @Fetch
  @RelatedTo(`type` = "REACHABLE_BY_ROCKET", direction = Direction.OUTGOING)
  var reachableByRocket: Set[World] = _

  def addRocketRouteTo(otherWorld: World) {
    reachableByRocket.add(otherWorld)
  }
}

object World {
    object RelTypes extends Enumeration {

      val REACHABLE_BY_ROCKET = new CustomRelationshipType("REACHABLE_BY_ROCKET")

      class CustomRelationshipType(val name: String) extends Val with RelationshipType

      implicit def convertValue(name: Value): CustomRelationshipType = name.asInstanceOf[CustomRelationshipType]
    }
}
