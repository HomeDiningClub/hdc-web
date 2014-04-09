package models

import java.util.Set;
import org.neo4j.graphdb.Direction
import org.neo4j.graphdb.RelationshipType
import org.springframework.data.annotation.PersistenceConstructor
import models.annotations.graph_annotations._
import scala.collection.mutable._

@NodeEntity
case class World @PersistenceConstructor() (
                                             @Indexed var name: String,
                                             @Indexed var moons: Int
                                             ){
  @GraphId var id: java.lang.Long = _
  var spokenLanguage: String = _

  // TODO Polish on enums
  // TODO Does not receive any data
  @Fetch
  @RelatedTo(`type` = "REACHABLE_BY_ROCKET", direction = Direction.OUTGOING)
  var reachableByRocket2: scala.collection.mutable.Set[World] = _

  @Fetch
  @RelatedTo(`type` = "REACHABLE_BY_ROCKET", direction = Direction.OUTGOING)
  var reachableByRocket: Set[World] = _

}

object World {
  object RelTypes extends Enumeration {
      val REACHABLE_BY_ROCKET = new CustomRelationshipType("REACHABLE_BY_ROCKET")
      class CustomRelationshipType(val name: String) extends Val with RelationshipType
      implicit def convertValue(name: Value): CustomRelationshipType = name.asInstanceOf[CustomRelationshipType]
    }
}
