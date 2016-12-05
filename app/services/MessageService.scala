package services

import java.util
import java.util.{Date, Set, UUID}
import javax.inject.{Inject, Named}

import models.message.{Message, MessageData}
import models.modelconstants.RelationshipTypesScala
import models.UserCredential
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.neo4j.annotation.Fetch
import org.springframework.data.neo4j.support.Neo4jTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import repositories.{MessageRepository, UserCredentialRepository}
import traits.TransactionSupport

import scala.collection.JavaConverters._

//@Named
//@Service
class MessageService @Inject()(val template: Neo4jTemplate,
                               val userCredentialRepository: UserCredentialRepository,
                               val messageRepository: MessageRepository) extends TransactionSupport {


  def createRequest(user: UserCredential, host: UserCredential, date: Date, time: Date, numberOfGuests: Int, request: String, phone: Option[String]): Message = withTransaction(template){

    var msg: Message = new Message
    msg.createMessage(date, time, numberOfGuests, request, user, host, user.firstName, host.firstName, RelationshipTypesScala.REQUEST.Constant, phone.getOrElse(""))
    //host.getMessages.add(saveMessage(msg))

    saveMessage(msg)
    //userCredentialRepository.save(host)

    msg
  }


  def createResponse(user: UserCredential, guest: UserCredential, messageObjectId: UUID, response: String, phone: String): Message = withTransaction(template){
    this.findById(messageObjectId) match {
      case None => null
      case Some(message) => createResponse(user,guest,message,response,phone)
    }
  }


  def createResponse(user: UserCredential, guest: UserCredential, message: Message, response: String, phone: String): Message = withTransaction(template){

    // "message" variable is the message that you are responding to
    var msg: Message = new Message
    msg.createMessage(message.date, message.time, message.numberOfGuests, response, user, guest, user.firstName, guest.firstName, RelationshipTypesScala.REPLY.Constant, phone)

    //guest.getMessages.add(saveMessage(msg))

    message.addResponse(msg)
    saveMessage(message)

    //userCredentialRepository.save(guest)

    msg
  }

  def findAllMessagesForUser(user: UserCredential): Option[List[Message]] = withTransaction(template){
    messageRepository.findAllMessagesForUser(user.objectId).asScala.toList match {
      case Nil => None
      case messages =>
        Some(messages)
    }
  }

  def findOutgoingMessagesForUser(user: UserCredential): Option[List[Message]] = withTransaction(template){
    messageRepository.findOutgoingMessagesForUser(user.objectId).asScala.toList match {
      case Nil => None
      case messages =>
        Some(messages)
    }
  }

  def findIncomingMessagesForUser(user: UserCredential): Option[List[MessageData]] = withTransaction(template){
    messageRepository.findIncomingMessagesForUser(user.objectId).asScala.toList match {
      case Nil =>
        None
      case messages =>
        Some(messages)
    }
  }

  def findById(id: UUID): Option[Message] = withTransaction(template){
    messageRepository.findByobjectId(id) match {
      case null => None
      case item => Some(item)
    }
  }

  def saveMessage(newItem: Message): Message = withTransaction(template){
    val newResult = messageRepository.save(newItem)
    newResult
  }

  def fetchMessage(message: Message): Message = withTransaction(template){
    template.fetch(message)
  }

  def saveUserCredentials(newItem: UserCredential): UserCredential = withTransaction(template){
    val newResult = userCredentialRepository.save(newItem)
    newResult
  }
}
