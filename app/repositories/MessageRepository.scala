package repositories

import java.util.UUID

import models.message.{Message}
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.repository.GraphRepository

trait MessageRepository extends GraphRepository[Message]{

  @Query("MATCH (n:`Message`) WHERE n.objectId={0} RETURN n")
  def findByobjectId(objectId: UUID): Message

  @Query("MATCH (n:`UserCredential`)-[r]-(m:`Message`) WHERE n.objectId={0} RETURN m ORDER BY m.createdDate DESC")
  def findAllMessagesForUser(objectId: UUID): java.util.List[Message]

  @Query("MATCH (n:`UserCredential`)-[r:`OUTGOING_MESSAGE`]->(m:`Message`) WHERE n.objectId={0} RETURN m ORDER BY m.createdDate DESC")
  def findOutgoingMessagesForUser(objectId: UUID): java.util.List[Message]

  @Query("MATCH (n:`UserCredential`)<-[r:`INCOMING_MESSAGE`]-(m:`Message`) WHERE n.objectId={0} RETURN m ORDER BY m.createdDate DESC")
  def findIncomingMessagesForUser(objectId: UUID): java.util.List[Message]

}
