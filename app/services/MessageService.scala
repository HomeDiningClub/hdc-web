package services

import java.util
import java.util.{UUID, Date, Set}

import models.message.{Message}
import models.modelconstants.RelationshipTypesScala
import models.{UserCredential}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.neo4j.annotation.Fetch
import org.springframework.data.neo4j.support.Neo4jTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import repositories.{MessageRepository, UserCredentialRepository}
import scala.collection.JavaConverters._

/**
 * Created by Tommy on 01/10/2014.
 */
@Service
class MessageService {

  @Autowired
  private var template: Neo4jTemplate = _

  @Autowired
  private var userCredentialRepository: UserCredentialRepository = _

  @Autowired
  private var messageRepository: MessageRepository = _

  @Transactional(readOnly = false)
  def createRequest(user: UserCredential, host: UserCredential, date: Date, time: Date, numberOfGuests: Int, request: String, phone: String): Message = {

    var msg: Message = new Message
    msg.createMessage(date, time, numberOfGuests, request, template.fetch(user), template.fetch(host), user.firstName, host.firstName, RelationshipTypesScala.REQUEST.Constant, phone)
    host.getMessages.add(saveMessage(msg))

    userCredentialRepository.save(host)

    msg
  }

  @Transactional(readOnly = false)
  def createResponse(user: UserCredential, guest: UserCredential, message: Message, response: String, phone: String): Message = {

    var msg: Message = new Message
    msg.createMessage(message.date, message.time, message.numberOfGuests, response, template.fetch(user), template.fetch(guest), user.firstName, guest.firstName, RelationshipTypesScala.REPLY.Constant, phone)

    message.response = msg
    msg.response = message

    guest.getMessages.add(saveMessage(msg))

    saveMessage(message)

    userCredentialRepository.save(guest)

    msg
  }

  @Transactional(readOnly = true)
  def findById(id: UUID): Message = {
    messageRepository.findByobjectId(id)
  }

  def saveMessage(newItem: Message): Message = {
    val newResult = messageRepository.save(newItem)
    newResult
  }

  @Transactional(readOnly = true)
  def fetchMessage(message: Message): Message = {
    template.fetch(message)
  }

  @Transactional(readOnly = false)
  def saveUserCredentials(newItem: UserCredential): UserCredential = {
    val newResult = userCredentialRepository.save(newItem)
    newResult
  }
}
