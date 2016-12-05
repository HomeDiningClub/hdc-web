package repositories

import java.util.UUID

import models.message.{Message, MessageData}
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.repository.GraphRepository

trait MessageRepository extends GraphRepository[Message]{

  @Query("MATCH (n:`Message`) WHERE n.objectId={0} RETURN n")
  def findByobjectId(objectId: UUID): Message

  @Query("MATCH (uc:`UserCredential`)-[r]-(m:`Message`) WHERE uc.objectId={0} RETURN m ORDER BY m.createdDate DESC")
  def findAllMessagesForUser(objectId: UUID): java.util.List[Message]

  @Query("MATCH (uc:`UserCredential`)-[r:`OUTGOING_MESSAGE`]->(m:`Message`) WHERE uc.objectId={0} RETURN m ORDER BY m.createdDate DESC")
  def findOutgoingMessagesForUser(objectId: UUID): java.util.List[Message]

  @Query("MATCH (recipientUp:`UserProfile`)<-[:`IN_PROFILE`]-(recipientUc:`UserCredential`)<-[:`INCOMING_MESSAGE`]-(m:`Message`)<-[:`OUTGOING_MESSAGE`]-(ownerUc:`UserCredential`)-[:`IN_PROFILE`]->(ownerUp:`UserProfile`) " +
    "WHERE recipientUc.objectId={0} " +
    "RETURN " +
    "recipientUc.objectId AS RecipientObjectId, " +
    "recipientUc.firstName AS RecipientFirstName, " +
    "recipientUc.lastName AS RecipientLastName, " +
    "recipientUp.profileLinkName AS RecipientProfileLinkName, " +
    "ownerUc.objectId AS OwnerObjectId, " +
    "ownerUc.firstName AS OwnerFirstName, " +
    "ownerUc.lastName AS OwnerLastName, " +
    "ownerUp.profileLinkName AS OwnerProfileLinkName, " +
    "m.objectId AS MessageObjectId, " +
    "m.createdDate AS CreatedDate, " +
    "m.phone AS PhoneNumber, " +
    "m.date AS RequestedDate, " +
    "m.time AS RequestedTime, " +
    "m.numberOfGuests AS NumberOfGuests, " +
    "m.request AS Request, " +
    "m.`type` AS MessageType, " +
    "m.read AS Read " +
    "ORDER BY m.createdDate DESC")
  def findIncomingMessagesForUser(objectId: UUID): java.util.List[MessageData]


}
