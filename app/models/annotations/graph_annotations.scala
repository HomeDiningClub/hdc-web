package models.annotations

import scala.annotation.meta.field

object graph_annotations {
  type GraphId = org.springframework.data.neo4j.annotation.GraphId @field
  type Id = org.springframework.data.annotation.Id @field
  type Indexed = org.springframework.data.neo4j.annotation.Indexed @field
  type Fetch = org.springframework.data.neo4j.annotation.Fetch @field
  type NodeEntity = org.springframework.data.neo4j.annotation.NodeEntity @field
  type RelatedTo = org.springframework.data.neo4j.annotation.RelatedTo @field
}
