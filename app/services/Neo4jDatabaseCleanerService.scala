package services

import java.util.Arrays
import java.util.HashMap
import java.util.Map

import org.springframework.data.neo4j.support.{Neo4jTemplate, DelegatingGraphDatabase, DelegatingGraphDatabaseGlobalOperations}
import org.springframework.data.neo4j.rest.SpringRestGraphDatabase
import org.neo4j.graphdb.{NotFoundException, Direction, Node, Relationship}
import org.neo4j.graphdb.index.{RelationshipIndex, Index}
import org.neo4j.rest.graphdb.index.RestIndex
import scala.collection.JavaConverters._
import org.springframework.stereotype.Service
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.neo4j.template.Neo4jOperations

@Service
class Neo4jDatabaseCleanerService {

  @Autowired
  private var template: Neo4jTemplate = _

  private def graph = {
    template.getGraphDatabase().asInstanceOf[SpringRestGraphDatabase]
  }

  def cleanDb(): Map[String, Any] = {
    val result = new HashMap[String, Any]()
    val tx = graph.beginTx()
    try {
      removeNodes(result)
      clearIndex(result)
      tx.success()
    } finally {
      tx.close()
    }
    result
  }

  private def removeNodes(result: Map[String, Any]) {
    //val refNode = getReferenceNodeOrNull
    var nodes = 0
    var relationships = 0
    for (node: Node <- graph.getAllNodes.asScala) {
      for (rel: Relationship <- node.getRelationships(Direction.OUTGOING).asScala) {
        rel.delete()
        relationships += 1
      }
      //if(node != refNode) {
        node.delete()
        nodes += 1
      //}
    }
    result.put("nodes", nodes)
    result.put("relationships", relationships)
  }

  private def getReferenceNodeOrNull(): Node = {
    try {
      graph.getReferenceNode(false)
    } catch {
      case e: NotFoundException => null
    }
  }

  private def clearIndex(result: Map[String, Any]) {
    val indexManager = graph.index()
    result.put("node_index", Arrays.asList(indexManager.nodeIndexNames():_*))
    result.put("relationship_index", Arrays.asList(indexManager.relationshipIndexNames():_*))
    for (ix <- indexManager.nodeIndexNames()) {
      deleteIndex(indexManager.forNodes(ix))
      //deleteRestIndex(indexManager.forNodes(ix))
    }
    for (ix <- indexManager.relationshipIndexNames()) {
      deleteRelIndex(indexManager.forRelationships(ix))
    }
  }

  private def deleteIndex(index: Index[Node]) {
    try {
      index.delete()
    } catch {
      case e: UnsupportedOperationException =>
    }
  }

  private def deleteRestIndex(index: RestIndex[Node]) {
    try {
      index.delete()
    } catch {
      case e: UnsupportedOperationException =>
    }
  }

  private def deleteRelIndex(index: RelationshipIndex) {
    try {
      index.delete()
    } catch {
      case e: UnsupportedOperationException =>
    }
  }

}