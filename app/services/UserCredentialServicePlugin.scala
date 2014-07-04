package services
// MATCH (tom) RETURN tom
// MATCH (tom) DELETE tom
// MATCH (tom) RETURN tom.providerId, tom.lastName, tom.firstName,tom.emailAddress

import models.{UserCredential, UserProfileData}
import repositories.UserCredentialRepository

import scala.collection.mutable.ListBuffer

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.neo4j.support.Neo4jTemplate
import org.neo4j.graphdb.index.Index
import org.neo4j.graphdb.Node
import securesocial.core._
import play.api.{Logger, Application}
import securesocial.core._
import securesocial.core.providers.Token
import securesocial.core.IdentityId
import org.springframework.data.neo4j.support.Neo4jTemplate
import org.springframework.beans.factory.annotation.Autowired
import repositories.UserProfileRepository
import org.springframework.stereotype.Service
import securesocial.core
import securesocial.core.providers.utils.BCryptPasswordHasher

/**
 * UserCredentialService
 * Methods for manages SecureSocial storage in Neo4J graphic database
 *
 * 1. Username/Password storage
 * 2. Facebook autentication
 * 3. Google autentication
 *
 */

class UserCredentialServicePlugin (application: Application) extends UserServicePlugin(application) {

  private var tokens = Map[String, Token]()
  var users = Map[String, Identity]()

  // Kontrollerar om id finns
  // dvs. userid och provider id genom att söka i databasen.

  def find(id: IdentityId): Option[Identity] = {

    val exitsUser = exists(id.userId, id.providerId)
    var uc  : UserCredential =  getuser(id.userId, id.providerId)

    if(exitsUser._2 == true){
      val returnUser = UserCredentialService.userCredential2socialUser(uc)
      return Some(returnUser)
    }

    None
  }



  // find by email and provider
  def findByEmailAndProvider(email: String, providerId: String): Option[Identity] = {

    var uc  : UserCredential =  getuser(email, providerId)
    val exitsUser = exists(email, providerId)

    if(exitsUser._2 == true){
      val returnUser = UserCredentialService.userCredential2socialUser(uc)
      return Some(returnUser)

    }
    None

  }





  // Kontrollera om elemen redan finns eller inte
  //  IdentityId = userId + providerId
  // om element redan finns skall id med dvs sättas.
  def save(user: Identity): Identity = {

    println("Method: Save ...................")

    // Kontrollera om användaren finns
    val exitsUser = exists(user.identityId.userId, user.identityId.providerId)

    println("Finns: " + exitsUser)

    // check if exists
    var uc  : UserCredential = getUserById(user.identityId.userId, user.identityId.providerId)

    println("Befitligt id: " + uc.id)


    var userCredential : UserCredential = UserCredentialService.socialUser2UserCredential(user)
    if(exitsUser._2 == true) {
      println("user finns")
    }

    var userCredential2 = createOrUpdateUser(userCredential)

    println("svar: " + userCredential2.firstName )

    //var uc2 : UserCredential = userCredentialService.socialUser2UserCredential(user)
    // Ett värde som säger om värdet redan finns i databasen uc.id

    user

  }


  /**
   * Checks if Identity exist meaning UserId and ProviderId
   * @param userId
   * @param providerId
   * @return id in Neo4j database and if the user exits true else false
   */
  @Transactional(readOnly = true)
  def exists(userId: String, providerId: String) :  (Long, Boolean) = {

    var user = getUserById(userId, providerId)
    var finns : Boolean = false
    var idNo  : Long    = -1L

    if(userId == null || providerId == null) {
      finns = false
      idNo = -1L
    } else if(user.userId == null || user.providerId == null) {
      finns = false
      idNo = -1L
    } else if(user.id != null && userId.equals(user.userId) && providerId.equals(user.providerId)) {
      finns = true
      idNo = user.id
    } else {
      finns = false
      idNo = -1L
    }

    val t = (idNo, finns)
    t
  }



  @Transactional(readOnly = true)
  def getUserById(userId: String, providerId: String) :  UserCredential = {

    var userCredential : UserCredential = new UserCredential()
    var list = getUserById(userId).filter(p=>p.providerId.equalsIgnoreCase(providerId))

    if(list.size > 0) {
      userCredential = list.head
    }

    userCredential
  }



  @Transactional(readOnly = true)
  def getUserById(userId: String) :  List[UserCredential] =  {
    val list : ListBuffer[UserCredential] = new ListBuffer[UserCredential]()
    var userCredentialIndex: Index[Node] = UserCredentialService.template.getIndex(classOf[UserCredential],"userId")
    var hits = userCredentialIndex.query("userId", userId)

    var nods = hits.iterator()
    var id : Long = 0
      while(nods.hasNext) {
      id = nods.next().getId
      var node : UserCredential = new UserCredential()
      node = UserCredentialService.userCredentialRepository.findOne(id)

      list += node
    }

    list.toList
  }



  /**
   * Get one UserCredential search on email and provider (facebook, gmail, ...)
   * @param emailAddress
   * @param providerId
   * @return
   */
  @Transactional(readOnly = true)
  def getuser(emailAddress: String, providerId: String) :  UserCredential =  {

    var userCredential : UserCredential = new UserCredential()
    var list = getUser(emailAddress).filter(p=>p.providerId.equalsIgnoreCase(providerId))

   if(list.size > 0) {
     userCredential = list.head
   }

    userCredential
  }




  /**
   * Get all list of UserCredential for
   * 1. Username and password
   * 2. Facebook
   * 3. Twiter
   *
   * @param emailAddress
   * @return
   */
  @Transactional(readOnly = true)
  def getUser(emailAddress: String) :  List[UserCredential] =  {
    val list : ListBuffer[UserCredential] = new ListBuffer[UserCredential]()
    var userCredentialIndex: Index[Node] = UserCredentialService.template.getIndex(classOf[UserCredential], "emailAddress")
    var hits = userCredentialIndex.query("emailAddress", emailAddress)

    var nods = hits.iterator()
    var id : Long = 0
    while(nods.hasNext) {
      id = nods.next().getId
      var node : UserCredential = new UserCredential()
      node = UserCredentialService.userCredentialRepository.findOne(id)

      list += node
    }

    list.toList
  }


  /**
   * Creates a new user or updates an existing user with
   * the same UserId and ProviderId, taking the key from the allready
   * existing user
   * @param userCredential
   * @return
   */
  @Transactional(readOnly = false)
  def createOrUpdateUser(userCredential: UserCredential): UserCredential = {
    val exitsUser = exists(userCredential.userId, userCredential.providerId)
    var modUserCredential: UserCredential = new UserCredential()

    if(exitsUser._2 == true) {
      userCredential.id = exitsUser._1
      modUserCredential = saveUser(userCredential)
    } else {
      modUserCredential = saveUser(userCredential)
    }

    modUserCredential
  }






  /**
   * Store a UserCredential
   * if id not assigned to a value insert create a new node
   * else update a node
   * @param userCredential
   * @return
   */
  @Transactional(readOnly = false)
  def saveUser(userCredential: UserCredential): UserCredential = {
    var modUser =  UserCredentialService.userCredentialRepository.save(userCredential)

    modUser
  }








  // Tokens
  def save(token: Token) {
    tokens += (token.uuid -> token)
  }

  def findToken(token: String): Option[Token] = {
    tokens.get(token)
  }

  def deleteToken(uuid: String) {
    tokens -= uuid
  }

  def deleteTokens() {
    tokens = Map()
  }

  def deleteExpiredTokens() {
    tokens = tokens.filter(!_._2.isExpired)
  }



}
