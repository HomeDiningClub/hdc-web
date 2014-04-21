package neo4j.models

import java.util.Set
import org.springframework.data.neo4j.annotation.GraphId
import org.springframework.data.neo4j.annotation.NodeEntity
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
//remove if not needed
import scala.collection.JavaConversions._

@NodeEntity
class AbstractNode {

  @GraphId
  var id: java.lang.Long = _

  @CreatedDate
  private var createdDate: java.lang.Long = _

  @LastModifiedDate
  private var lastModifiedDate: java.lang.Long = _
}
