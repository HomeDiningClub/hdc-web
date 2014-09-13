package services

import java.util.UUID

import models.entity.EmptyNode
import org.neo4j.graphdb.Node
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.neo4j.support.Neo4jTemplate
import org.springframework.stereotype.Service
import repositories.NodeEntityRepository

@Service
class NodeEntityService {

  @Autowired
  var template: Neo4jTemplate = _

  @Autowired
  private var nodeEntityRepository: NodeEntityRepository = _

  def getAnyNodeUsingId(objectId: UUID): Option[EmptyNode] = {

    nodeEntityRepository.getAnyNodeUsingId(objectId) match {
      case null => None
      case anyNode => Some(anyNode)
    }
  }
}
