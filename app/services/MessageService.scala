package services

import java.util
import java.util.{UUID, Date, Set}

import models.message.{Message}
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
  def createRequest(requestingUser: UserCredential, hostingUser: UserCredential, date: Date, time: Date, numberOfGuests: Int, request: String): Message = {

    var msg: Message = new Message
    msg.createMessage(date, time, numberOfGuests, request, template.fetch(requestingUser), "")
    hostingUser.getMessages.add(saveMessage(msg))

    userCredentialRepository.save(hostingUser)

    msg
  }

  @Transactional(readOnly = false)
  def createResponse(user: UserCredential, guest: UserCredential, message: Message, response: String): Message = {

    var msg: Message = new Message
    msg.createMessage(message.date, message.time, message.numberOfGuests, response, template.fetch(guest), (user.firstName + " " + user.lastName))

    message.response = msg

    guest.getMessages.add(saveMessage(msg))

    saveMessage(message)

    userCredentialRepository.save(guest)

    msg
  }

  def saveMessage(newItem: Message): Message = {
    val newResult = messageRepository.save(newItem)
    newResult
  }

  @Transactional(readOnly = false)
  def saveUserCredentials(newItem: UserCredential): UserCredential = {
    val newResult = userCredentialRepository.save(newItem)
    newResult
  }
}
