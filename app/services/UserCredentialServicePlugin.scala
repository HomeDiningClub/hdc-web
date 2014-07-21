package services

// Get UserCredential
// MATCH (a:UserCredential) RETURN a
// MATCH (a:UserCredential) RETURN a.providerId, a.lastName, a.firstName, a.emailAddress

// Gooogle
// MATCH (a:UserCredential {providerId:'google'}) RETURN a.providerId, a.lastName, a.firstName, a.emailAddress
// facebook
// MATCH (a:UserCredential {providerId:'facebook'}) RETURN a.providerId, a.lastName, a.firstName, a.emailAddress

// Fetch all nodes
// MATCH (tom) RETURN tom

// Delete all nodes
// MATCH (tom) DELETE tom

import _root_.java.util.UUID
import models.{UserCredential, UserProfileData}
import models.UserProfile
import repositories.UserProfileRepository

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
 */
class UserCredentialServicePlugin (application: Application) extends UserServicePlugin(application) {

  private var tokens = Map[String, Token]()
  var users = Map[String, Identity]()





  /** *
    * Fetch user by IdentityId meaning userId and ProviderId
    * from the database
    * @param id
    * @return
    */
  def find(id: IdentityId): Option[UserCredential] = {

    // check databaseId and true/false
    val exitsUser = exists(id.userId, id.providerId)
    // Fetch user
    val uc : UserCredential = getuser(id.userId, id.providerId)

    if(exitsUser._2 == true){
      //val returnUser = UserCredentialService.userCredential2socialUser(uc)
      return Some(uc)
    }

    None
  }


  /**
   * Fetch by email and providerId (facebook/google/....)
   * @param email email
   * @param providerId provider for exampel facebook, google
   * @return UserCredential
   */
  def findByEmailAndProvider(email: String, providerId: String): Option[UserCredential] = {

    val uc : UserCredential = getuser(email, providerId)
    val exitsUser = exists(email, providerId)

    if(exitsUser._2 == true){
      //val returnUser = UserCredentialService.userCredential2socialUser(uc)
      return Some(uc)

    }
    None
  }


  /** *
    * Store Identity in the database
    * @param user
    * @return
    */
  def save(user: Identity): Identity = {

    var service = new services.UserProfileService()

   println("userId : " + user.identityId.userId)
    println("ProviderId : " + user.identityId.providerId)

    var userProfile : Option[UserProfile] = service.findUserProfileByUserId(user)



    if(userProfile == None) {

      println("Hittade inte : " + user.identityId.userId)

      var up: UserProfile = new UserProfile()
      up.providerIdentity = user.identityId.providerId
      up.userIdentity = user.identityId.userId
      up.keyIdentity = user.identityId.userId + "_" + user.identityId.providerId

      service.saveUserProfile(up)
    } else {
      println("Key id: " +  userProfile.getOrElse(new UserProfile).keyIdentity)
    }

    val userCredential : UserCredential = UserCredentialService.socialUser2UserCredential(user)
    val userCredential2 = createOrUpdateUser(userCredential)
    userCredential2
  }


  /**
   * Checks if Identity exist meaning UserId and ProviderId
   * @param userId
   * @param providerId
   * @return id in Neo4j database and if the user exits true else false
   */
  @Transactional(readOnly = true)
  def exists(userId: String, providerId: String) :  (UUID, Boolean, Long) = {

    var user = getUserById(userId, providerId)
    var finns : Boolean = false
    var idNo  : UUID    = null
    var graphId : Long = -1L

    if(userId == null || providerId == null) {
      finns = false
      idNo = null
      graphId = -1L
      throw new Exception("UserId or ProviderId could not be null")
    } else if(user.userId == null || user.providerId == null) {
      // Could not find anything on UserId and ProviderId
      finns = false
      idNo = null
      graphId = -1L
    } else if(user.objectId != null && userId.equals(user.userId) && providerId.equals(user.providerId)) {
      finns = true
      idNo = user.objectId
      graphId = user.graphId
    } else {
      finns = false
      idNo = null
      graphId = -1L
    }

    val t = (idNo, finns, graphId)
    t
  }


  /***
  * Return the UserCredentials with the userId and providerId by
  * first fetching on userid and filter out the right providerid
  * assures that it is online one answer
  */
  @Transactional(readOnly = true)
  def getUserById(userId: String, providerId: String) :  UserCredential = {

    var userCredential : UserCredential = new UserCredential()
    val list = getUserById(userId).filter(p=>p.providerId.equalsIgnoreCase(providerId))

    if(list.size > 0) {
      userCredential = list.head
    }

    userCredential
  }


  /***
   * Get the credetials for one userid
   */
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
            // Fetch by id
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
   * 3. Google
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
   * the same UserId and ProviderId.
   *  Checks if the same user credentials already is stored meaning then same
   *  userid and provider id when update the existing one othervice create
   *  a new user.
   *
   * @param userCredential
   * @return modified userCredential
   */
  @Transactional(readOnly = false)
  def createOrUpdateUser(userCredential: UserCredential): UserCredential = {

    // check if the same userId and providerId is already stored in the database
    val exitsUser = exists(userCredential.userId, userCredential.providerId)

    // creates the return type
    var modUserCredential: UserCredential = new UserCredential()

    if(exitsUser._2 == true) {
        // User is already stored in the database, when update
        println("update id: " + userCredential.objectId + "email : " + userCredential.emailAddress)

        // set the correct id
        userCredential.objectId = exitsUser._1
        userCredential.graphId = exitsUser._3
        modUserCredential = saveUser(userCredential)
    } else {

        println("create, email : " + userCredential.emailAddress)
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
  private def saveUser(userCredential: UserCredential): UserCredential = {
    val modUser = UserCredentialService.userCredentialRepository.save(userCredential)
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
