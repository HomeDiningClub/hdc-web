package services

import javax.inject.{Named,Inject}

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.neo4j.support.Neo4jTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import repositories._
import models.{UserCredential, UserRole}
import traits.TransactionSupport
import scala.collection.JavaConverters._
import java.util.UUID
import enums.RoleEnums.RoleEnums

//@Named
//@Service
class UserRoleService @Inject() (val template: Neo4jTemplate,
                                 val userRoleRepository: UserRoleRepository,
                                 val userCredentialService: UserCredentialService) extends TransactionSupport {

  /*
  @Autowired
  private var userRoleRepository: UserRoleRepository = _

  @Autowired
  private var userCredentialService: UserCredentialService = _
*/

  def findByName(name: String): Option[UserRole] = {
    val retItem = userRoleRepository.findByname(name)
    if(retItem.isInstanceOf[UserRole])
      Some(retItem)
    else
      None
  }

  def createRole(name: String): UserRole = {
    save(new UserRole(name.toUpperCase))
  }

  def createRole(name: RoleEnums): UserRole = {
    save(new UserRole(name.toString))
  }


  @Transactional(readOnly = true)
  def findById(objectId: UUID): UserRole = {
    userRoleRepository.findByobjectId(objectId)
  }

  @Transactional(readOnly = true)
  def getListOfAll: Option[List[UserRole]] = withTransaction(template) {
    val listOfAll: List[UserRole] = userRoleRepository.findAll().iterator.asScala.toList

    if(listOfAll.isEmpty)
      None
    else
      Some(listOfAll)
  }

  @Transactional(readOnly = false)
  def deleteById(objectId: UUID): Boolean = {
    val item: UserRole = this.findById(objectId)
    if(item != null)
    {
      userRoleRepository.delete(item)
      return true
    }
    false
  }

  @Transactional(readOnly = false)
  def deleteAll() {
    userRoleRepository.deleteAll()
  }

  @Transactional(readOnly = false)
  def addRoleToUser(roleObjectId: UUID, userObjectId: UUID): UserCredential = {
    val role = findById(roleObjectId)
    val user = userCredentialService.findById(userObjectId).get
    user.roles.add(role)
    val modUser = userCredentialService.userCredentialRepository.save(user)
    modUser
  }

  @Transactional(readOnly = false)
  def removeRoleFromUser(roleObjectId: UUID, userObjectId: UUID): UserCredential = {
    val role = findById(roleObjectId)
    val user = userCredentialService.findById(userObjectId).get
    user.roles.remove(role)
    val modUser = userCredentialService.userCredentialRepository.save(user)
    modUser
  }

  @Transactional(readOnly = false)
  def save(newContent: UserRole): UserRole = {
    // Don't add if already exists, just return the existing instance
    val role = findByName(newContent.name) match {
      case Some(role) => role
      case None => userRoleRepository.save(newContent)
    }
    role
  }


}
