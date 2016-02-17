package services

import java.util
import java.util.{UUID, Date, Set}
import javax.inject.{Inject, Named}

import models.message.{Message}
import models.modelconstants.RelationshipTypesScala
import models.{UserCredential}
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
/*
  @Autowired
  private var template: Neo4jTemplate = _

  @Autowired
  private var userCredentialRepository: UserCredentialRepository = _

  @Autowired
  private var messageRepository: MessageRepository = _
*/

  //@Transactional(readOnly = false)
  def createRequest(user: UserCredential, host: UserCredential, date: Date, time: Date, numberOfGuests: Int, request: String, phone: Option[String]): Message = withTransaction(template){

    var msg: Message = new Message
    msg.createMessage(date, time, numberOfGuests, request, user, host, user.firstName, host.firstName, RelationshipTypesScala.REQUEST.Constant, phone.getOrElse(""))
    //host.getMessages.add(saveMessage(msg))

    saveMessage(msg)
    //userCredentialRepository.save(host)

    msg
  }

  //@Transactional(readOnly = false)
  def createResponse(user: UserCredential, guest: UserCredential, message: Message, response: String, phone: String): Message = withTransaction(template){
    // message variabel är orginal meddelandet som man svarar på
    var msg: Message = new Message
    msg.createMessage(message.date, message.time, message.numberOfGuests, response, user, guest, user.firstName, guest.firstName, RelationshipTypesScala.REPLY.Constant, phone)

    //guest.getMessages.add(saveMessage(msg))

    message.addResponse(msg)
    saveMessage(message)

    //userCredentialRepository.save(guest)

    msg
  }

  //@Transactional(readOnly = true)
  def findAllMessagesForUser(user: UserCredential): Option[List[Message]] = withTransaction(template){
    messageRepository.findAllMessagesForUser(user.objectId).asScala.toList match {
      case Nil => None
      case messages =>
        Some(messages)
    }
  }

  //@Transactional(readOnly = true)
  def findOutgoingMessagesForUser(user: UserCredential): Option[List[Message]] = withTransaction(template){
    messageRepository.findOutgoingMessagesForUser(user.objectId).asScala.toList match {
      case Nil => None
      case messages =>
        Some(messages)
    }
  }

  //@Transactional(readOnly = true)
  def findIncomingMessagesForUser(user: UserCredential): Option[List[Message]] = withTransaction(template){
    messageRepository.findIncomingMessagesForUser(user.objectId).asScala.toList match {
      case Nil => None
      case messages =>
        Some(messages)
    }
  }

  //@Transactional(readOnly = true)
  def findById(id: UUID): Option[Message] = withTransaction(template){
    messageRepository.findByobjectId(id) match {
      case null => None
      case item => Some(item)
    }
  }

  //@Transactional(readOnly = false)
  def saveMessage(newItem: Message): Message = withTransaction(template){
    val newResult = messageRepository.save(newItem)
    newResult
  }

  //@Transactional(readOnly = true)
  def fetchMessage(message: Message): Message = withTransaction(template){
    template.fetch(message)
  }

  //@Transactional(readOnly = false)
  def saveUserCredentials(newItem: UserCredential): UserCredential = withTransaction(template){
    val newResult = userCredentialRepository.save(newItem)
    newResult
  }
}
