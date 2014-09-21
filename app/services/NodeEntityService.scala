package services

import java.util.UUID

import interfaces.IEditable
import models.UserCredential
import models.entity.EmptyNode
import org.apache.commons.collections.map.HashedMap
import org.neo4j.graphdb.Node
import org.neo4j.helpers.collection.MapUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.neo4j.conversion.Result
import org.springframework.data.neo4j.support.Neo4jTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import repositories.NodeEntityRepository

@Service
class NodeEntityService {

  @Autowired
  var template: Neo4jTemplate = _

  @Autowired
  private var nodeEntityRepository: NodeEntityRepository = _

  def getAnyNodeUsingId(objectId: UUID): Option[Node] = {

    val params = MapUtil.map("objectId",objectId.toString)
    val query = "START n=node(*) WHERE HAS (n.objectId) AND n.objectId={objectId} RETURN n"
//    val query = "START x=node:searchByUserName(userName = {userAUserName}), " +
//      "y=node:searchByUserName(userName = {userBUserName})" +
//      " MATCH (x)-[r:FOLLOWS]->(y)" +
//      " SET r.currentValue = {value}" +
//      " RETURN r"
    val queryRes = template.query(query, params).to(classOf[Node]).singleOrNull()

    queryRes match {
      case null =>
        None
      case anyNode =>
        Some(anyNode)
    }
    //.to(classOf[EmptyNode]).single match {

//    nodeEntityRepository.getAnyNodeUsingId(objectId.toString) match {
//      case null => None
//      case anyNode => Some(anyNode)
//    }
  }

  @Transactional(readOnly = true)
  def isNodeEditableBy(anyItem: Node, userCred: UserCredential): Boolean = {
    InstancedServices.nodeEntityService.template.findOne(anyItem.getId,InstancedServices.userCredentialService.template.getStoredEntityType(anyItem).getType).asInstanceOf[IEditable].isEditableBy(userCred.objectId).asInstanceOf[Boolean]
  }
}
