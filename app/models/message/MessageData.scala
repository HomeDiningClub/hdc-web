package models.message

import java.util
import java.util.Date

import org.springframework.data.neo4j.annotation.{QueryResult, ResultColumn}

@QueryResult
trait MessageData {

  // Owner (Sender of original message)
  @ResultColumn("OwnerObjectId")
  def getOwnerObjectId() : String

  @ResultColumn("OwnerFirstName")
  def getOwnerFirstName() : String

  @ResultColumn("OwnerLastName")
  def getOwnerLastName() : String

  @ResultColumn("OwnerProfileLinkName")
  def getOwnerProfileLinkName() : String


  // Recipient
  @ResultColumn("RecipientObjectId")
  def getRecipientObjectId() : String

  @ResultColumn("RecipientFirstName")
  def getRecipientFirstName() : String

  @ResultColumn("RecipientLastName")
  def getRecipientLastName() : String

  @ResultColumn("RecipientProfileLinkName")
  def getRecipientProfileLinkName() : String



  // Message
  @ResultColumn("MessageObjectId")
  def getMessageObjectId() : String

  @ResultColumn("CreatedDate")
  def getCreatedDate() : Date

  @ResultColumn("RequestedDate")
  def getRequestedDate() : Date

  @ResultColumn("RequestedTime")
  def getRequestedTime() : Date

  @ResultColumn("PhoneNumber")
  def getPhoneNumber() : String

  @ResultColumn("NumberOfGuests")
  def getNumberOfGuests() : Int

  @ResultColumn("Request")
  def getRequest() : String

  @ResultColumn("MessageType")
  def getMessageType() : String

  @ResultColumn("Read")
  def getRead() : Boolean

}

