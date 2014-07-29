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
import enums.RoleEnums
import enums.RoleEnums.RoleEnums
import models.{UserCredential, UserProfileData}
import models.UserProfile
import org.neo4j.helpers.collection.IteratorUtil
import repositories.UserProfileRepository

import scala.collection.JavaConverters._

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
import enums.RoleEnums
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
    val existsUser = exists(id.userId, id.providerId)

    // Fetch user
    //var uc  = getuser(id.userId, id.providerId)
    // var uc = findByEmailAndProvider(id.userId, id.providerId)
    var uc =  findByUserIdAndProviderId(id.userId, id.providerId)


    if(existsUser._2){
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

    val uc : Option[UserCredential] = Some(getUserByEmailAndProvider(email, providerId))
    val exitsUser = exists(email, providerId)


    if(exitsUser._2 == true){

      return uc

    }

    None
  }




  /** *
    * Store Identity in the database
    * @param user
    * @return
    */
  def save(user: Identity): Identity = {
    val userCredential : UserCredential = InstancedServices.userCredentialService.socialUser2UserCredential(user)
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

    var user = findByUserIdAndProviderId(userId, providerId)
    var finns : Boolean = false
    var idNo  : UUID    = null
    var graphId : Long = -1L

    if(user == null) {
      finns = false
      idNo = null
      graphId = -1L
    }
    else if(userId == null || providerId == null) {
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
  /*
  @Transactional(readOnly = true)
  def getUserById(userId: String, providerId: String) :  UserCredential = {

    var userCredential : UserCredential = new UserCredential()
    val list = getUserById(userId).filter(p=>p.providerId.equalsIgnoreCase(providerId))

    println("Number of userids : " +list.size)

    if(list.size > 0) {
      userCredential = list.head
      println("FirstName : " + userCredential.firstName())
      println("UserId : " + userCredential.identityId().userId)
      println("ProviderId : " + userCredential.identityId().providerId)
    }

    userCredential
  }
*/

  /***
   * Get the credetials for one userid
   */
  /*
  @Transactional(readOnly = true)
  def getUserById(userId: String) :  List[UserCredential] =  {

    val list : ListBuffer[UserCredential] = new ListBuffer[UserCredential]()

    var userCredentialIndex: Index[Node] = InstancedServices.userCredentialService.template.getIndex(classOf[UserCredential],"userId")
    var hits = userCredentialIndex.query("userId", userId)

    var nods = hits.iterator()
    var id : Long = 0

      while(nods.hasNext) {
            id = nods.next().getId
            var node : UserCredential = new UserCredential()
            // Fetch by id
            node = InstancedServices.userCredentialService.userCredentialRepository.findOne(id)
            list += node
      }

    list.toList
  }
*/


  /**
   * Fetch an UserCredential by searching on userId and providerId
   * @param userId id for the Authentication the user
   * @param providerId the service used to authenticate the user
   * @return UserCredential information to be able to authenticate the user
   */
  @Transactional(readOnly = true)
  def findByUserIdAndProviderId(userId: String, providerId: String) :  UserCredential =  {
    var user = InstancedServices.userCredentialService.userCredentialRepository.findByuserIdAndProviderId(userId,providerId)
    return user
  }

  // Fetch user by emailAddress and providerId
  @Transactional(readOnly = true)
  def getUserByEmailAndProvider(emailAddress: String, providerId: String) :  UserCredential =  {
    var user = InstancedServices.userCredentialService.userCredentialRepository.findByemailAddressAndProviderId(emailAddress, providerId)
    return user
  }


  // List alla users
  @Transactional(readOnly = true)
  def getUsers() :  List[UserCredential] =  {
    IteratorUtil.asCollection(InstancedServices.userCredentialService.userCredentialRepository.findAll()).asScala.toList
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
    var modUserCredential: UserCredential = findByUserIdAndProviderId(userCredential.userId, userCredential.providerId)

    if(exitsUser._2 == true) {
        // User is already stored in the database, when update
        println("update id: " + userCredential.objectId + "email : " + userCredential.emailAddress)

      println("objectId = " + modUserCredential.objectId)
      println("graphId = " + modUserCredential.graphId)

      println("reles : ")
      var itRoles = modUserCredential.roles.iterator()
      while(itRoles.hasNext) {
        var rol = itRoles.next()
        println("Role : " + rol.name)
      }


      modUserCredential.oAuth1InfoToken         = userCredential.oAuth1InfoToken
      modUserCredential.oAuth1InfoSecret        = userCredential.oAuth1InfoSecret
      modUserCredential.oAuth2InfoAccessToken   = userCredential.oAuth2InfoAccessToken
      modUserCredential.oAuth2InfoExpiresIn     = userCredential.oAuth2InfoExpiresIn
      modUserCredential.oAuth2InfoRefreshToken  = userCredential.oAuth2InfoRefreshToken
      modUserCredential.oAuth2InfoTokenType     = userCredential.oAuth2InfoTokenType
      modUserCredential.password                = userCredential.password
      modUserCredential.authMethod              = userCredential.authMethod
      modUserCredential.firstName               = userCredential.firstName
      modUserCredential.lastName                =  userCredential.lastName
      modUserCredential.fullName                = userCredential.fullName
      modUserCredential.salt                    = userCredential.salt
      modUserCredential.hasher                  = userCredential.hasher
        var newUserCredential                   = saveUser(modUserCredential)
        return newUserCredential

    } else {

        println("create userCredential, UserId : " + userCredential.userId)
        // Add default group
        InstancedServices.userCredentialService.addRole(userCredential, RoleEnums.USER)
        var newUserCredential = saveUser(userCredential)
       return newUserCredential
    }


  }

  @Transactional(readOnly = false)
  def addRole(userCredential: UserCredential, role: RoleEnums): UserCredential = {

    // check if the same userId and providerId is already stored in the database
    val exitsUser = exists(userCredential.userId, userCredential.providerId)

    // creates the return type
    var modUserCredential: UserCredential = findByUserIdAndProviderId(userCredential.userId, userCredential.providerId)

    if(exitsUser._2 == true) {
      // User is already stored in the database, when update
      println("update id: " + userCredential.objectId + "email : " + userCredential.emailAddress)

      println("objectId = " + modUserCredential.objectId)
      println("graphId = " + modUserCredential.graphId)

      println("reles : ")
      var itRoles = modUserCredential.roles.iterator()
      while(itRoles.hasNext) {
        var rol = itRoles.next()
        println("Role : " + rol.name)
      }


      modUserCredential.oAuth1InfoToken         = userCredential.oAuth1InfoToken
      modUserCredential.oAuth1InfoSecret        = userCredential.oAuth1InfoSecret
      modUserCredential.oAuth2InfoAccessToken   = userCredential.oAuth2InfoAccessToken
      modUserCredential.oAuth2InfoExpiresIn     = userCredential.oAuth2InfoExpiresIn
      modUserCredential.oAuth2InfoRefreshToken  = userCredential.oAuth2InfoRefreshToken
      modUserCredential.oAuth2InfoTokenType     = userCredential.oAuth2InfoTokenType
      modUserCredential.password                = userCredential.password
      modUserCredential.authMethod              = userCredential.authMethod
      modUserCredential.firstName               = userCredential.firstName
      modUserCredential.lastName                =  userCredential.lastName
      modUserCredential.fullName                = userCredential.fullName
      modUserCredential.salt                    = userCredential.salt
      modUserCredential.hasher                  = userCredential.hasher

      InstancedServices.userCredentialService.addRole(userCredential, role)

      var newUserCredential                   = saveUser(modUserCredential)
      return newUserCredential

    } else {

      println("create userCredential, UserId : " + userCredential.userId)
      // Add default group
      InstancedServices.userCredentialService.addRole(userCredential, role)
      var newUserCredential = saveUser(userCredential)
      return newUserCredential
    }


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
    val modUser = InstancedServices.userCredentialService.userCredentialRepository.save(userCredential)
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
