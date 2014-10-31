package services

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
  var users = Map[String, UserCredential]()

  var isUserCacheON : Boolean = true


  /** *
    * Fetch user by IdentityId meaning userId and ProviderId
    * from the database
    * @param id
    * @return
    */
  def find(id: IdentityId): Option[UserCredential] = {
    var log : Long = utils.Helpers.startPerfLog()
    var uc =  findByUserIdAndProviderId(id.userId, id.providerId)

    utils.Helpers.endPerfLog("UserCredentialServicePlugin - find with id: " + id.userId, log)

   if(uc != null && uc != None) {
     Some(uc)
   } else {
     None
   }

  }



  def findByEmailAndProvider(email: String, providerId: String): Option[UserCredential] = {

    var log : Long = utils.Helpers.startPerfLog()

    val uc : Option[UserCredential] = Some(getUserByEmailAndProvider(email.toLowerCase, providerId))

    var isFoundEmail : Boolean = false
    if(uc == null || uc == None) {
      isFoundEmail = false
    } else {
      isFoundEmail = true
    }

    //val exitsUser = exists(email, providerId)

    utils.Helpers.endPerfLog("findByEmailAndProvider - find with id: " + email, log)


    if(isFoundEmail){

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
    var log : Long = utils.Helpers.startPerfLog()
    val userCredential : UserCredential = InstancedServices.userCredentialService.socialUser2UserCredential(user)
    val userCredential2 = createOrUpdateUser(userCredential)
    utils.Helpers.endPerfLog("save - find with id: " + user.email, log)
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

    var log : Long = utils.Helpers.startPerfLog()

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
    } else if(user.objectId != null && userId.equalsIgnoreCase(user.userId) && providerId.equalsIgnoreCase(user.providerId)) {
      finns = true
      idNo = user.objectId
      graphId = user.graphId
    } else {
      finns = false
      idNo = null
      graphId = -1L
    }

    utils.Helpers.endPerfLog("exists - find with id: " + userId, log)

    val t = (idNo, finns, graphId)
    t
  }







  /**
   * Fetch an UserCredential by searching on userId and providerId
   * @param userId id for the Authentication the user
   * @param providerId the service used to authenticate the user
   * @return UserCredential information to be able to authenticate the user
   */
  @Transactional(readOnly = true)
  def findByUserIdAndProviderId(userId: String, providerId: String) :  UserCredential =  {

    var mUserId : String = userId.toLowerCase

  // search in user cache
    var u = findUserCache(userId, providerId)

    if(u != null && u != None && u.get != null && u.get != None && isUserCacheON) {
      return u.get
    }

    var user = InstancedServices.userCredentialService.userCredentialRepository.findByuserIdAndProviderId(mUserId,providerId)

    if(user != null && user != None && isUserCacheON) {
      addUserCache(user)
    }


    return user
  }

  // Fetch user by emailAddress and providerId
  @Transactional(readOnly = true)
  def getUserByEmailAndProvider(emailAddress: String, providerId: String) :  UserCredential =  {
    var lowerCaseEmailAddress = emailAddress.toLowerCase
   // var user = InstancedServices.userCredentialService.userCredentialRepository.findByemailAddressAndProviderId(lowerCaseEmailAddress, providerId)
   var user = InstancedServices.userCredentialService.userCredentialRepository.findByemailAddressAndProviderId2(lowerCaseEmailAddress, providerId)
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

    var log : Long = utils.Helpers.startPerfLog()

    var userId = userCredential.userId

    // only done if login is done by username and password
    if(userCredential.providerId.equalsIgnoreCase("userpass"))
    {
      userId = userCredential.userId.toLowerCase()
    }


    // check if the same userId and providerId is already stored in the database
   // val exitsUser = exists(userCredential.userId, userCredential.providerId)

    // creates the return type
    var modUserCredential: UserCredential = findByUserIdAndProviderId(userCredential.userId, userCredential.providerId)

    var userExits : Boolean = false
    if(modUserCredential == null || modUserCredential == None) {
      userExits = false
    } else {
      userExits = true
    }



    if(userExits) {

        // User is already stored in the database, when update
        var itRoles = modUserCredential.roles.iterator()
        while(itRoles.hasNext) {
          var rol = itRoles.next()
        }

        var profileItter = modUserCredential.profiles.iterator()
        if(profileItter.hasNext)
        {
          var theProfile = profileItter.next()
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

     // personnummer is not given here
     // modUserCredential.personNummer            = userCredential.personNummer

        var newUserCredential                   = saveUser(modUserCredential)

      utils.Helpers.endPerfLog("createOrUpdateUser - update", log)
        return newUserCredential

    } else {
        // Add default group
        InstancedServices.userCredentialService.addRole(userCredential, RoleEnums.USER)
        var userProfile : UserProfile = new UserProfile()

      userProfile.userIdentity = userCredential.userId
      userProfile.providerIdentity = userCredential.providerId
      userProfile.fistName = userCredential.firstName
      userProfile.lastName = userCredential.lastName
      userProfile.keyIdentity = userProfile.userIdentity + "_" + userProfile.providerIdentity

      userCredential.userId = userId // lowercase
      userCredential.emailAddress = userCredential.emailAddress.toLowerCase // lowcase

      var storedUserProfile = InstancedServices.userProfileService.saveUserProfile(userProfile)
      InstancedServices.userCredentialService.addUserProfile(userCredential, storedUserProfile)
      var newUserCredential = saveUser(userCredential)


      utils.Helpers.endPerfLog("createOrUpdateUser insert", log)
        return newUserCredential
    }


  }

  @Transactional(readOnly = false)
  def addRole(userCredential: UserCredential, role: RoleEnums): UserCredential = {

    var log : Long = utils.Helpers.startPerfLog()

    // check if the same userId and providerId is already stored in the database
    val exitsUser = exists(userCredential.userId, userCredential.providerId)

    // creates the return type
    var modUserCredential: UserCredential = findByUserIdAndProviderId(userCredential.userId, userCredential.providerId)

    if(exitsUser._2 == true) {
      // User is already stored in the database, when update
      var itRoles = modUserCredential.roles.iterator()
      while(itRoles.hasNext) {
        var rol = itRoles.next()
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

      utils.Helpers.endPerfLog("addRole", log)
      return newUserCredential

    } else {
      // Add default group
      InstancedServices.userCredentialService.addRole(userCredential, role)
      var newUserCredential = saveUser(userCredential)
      utils.Helpers.endPerfLog("addRole", log)
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

    // cache ...
    if(isUserCacheON) {
      deleteUserCache(userCredential.userId, userCredential.providerId)
    }
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


  def addUserCache(user : UserCredential) {
    var key : String = user.userId.toLowerCase + "-" + user.providerId
    users += (key->user)
  }

  def deleteUserCache(userId : String, providerId: String) {
    var key : String = userId.toLowerCase + "-" + providerId
     users -= key
  }

  def findUserCache(userId : String, providerId: String) : Option[UserCredential] = {
    var key : String = userId.toLowerCase + "-" + providerId
    users.get(key)
  }


}
