package services

import _root_.java.util.UUID
import javax.inject.{Named, Inject}
import enums.RoleEnums.RoleEnums
import models.{ViewedByUnKnown, UserCredential, UserProfile}
import org.neo4j.helpers.collection.IteratorUtil
import play.api.Logger
import securesocial.core.BasicProfile
import securesocial.core.services.UserService
import scala.collection.JavaConverters._
import org.springframework.transaction.annotation.Transactional
import enums.RoleEnums
import scala.concurrent.Future
import securesocial.core.providers.MailToken

class UserCredentialServicePlugin extends UserService[UserCredential] {

  //val injector = Guice.createInjector(new SpringNeo4jModule)

  def userProfileService: UserProfileService = {
    play.api.Play.current.injector.instanceOf(classOf[UserProfileService])
    //injector.getInstance(classOf[UserProfileService])
  }

  def userCredentialService: UserCredentialService = {
    play.api.Play.current.injector.instanceOf(classOf[UserCredentialService])
    //injector.getInstance(classOf[UserCredentialService])
  }

  private var tokens = Map[String, MailToken]()
  var users = Map[String, UserCredential]()
  var isUserCacheON : Boolean = false /// @todo
  //implicit val implicitEnv = env


 /* /** *
    * Fetch user by IdentityId meaning userId and ProviderId
    * from the database
    * @param id
    * @return
    */
  def find(id: securesocial.core.GenericProfile): Option[UserCredential] = {
    //var log : Long = customUtils.Helpers.startPerfLog()
    var uc =  findByUserIdAndProviderId(id.userId, id.providerId)

    //customUtils.Helpers.endPerfLog("UserCredentialServicePlugin - find with id: " + id.userId, log)

    if(uc != null && uc != None) {
     Some(uc)
    } else {
     None
    }

  }
*/
  def link(current: UserCredential, to: securesocial.core.BasicProfile): Future[UserCredential] = {
    // We don't support more than one login at this time, please implement a list of BasicProfiles on UserCredential and update all the lookup methods in this class

    /*
    if (current.identities.exists(i => i.providerId == to.providerId && i.userId == to.userId)) {
      Future.successful(current)
    } else {
      val added = to :: current.identities
      val updatedUser = current.copy(identities = added)
      users = users + ((current.main.providerId, current.main.userId) -> updatedUser)
      Future.successful(updatedUser)
    }
    */
    Future.successful(current)
  }

  def passwordInfoFor(user: UserCredential): Future[Option[securesocial.core.PasswordInfo]] = {
    // We don't support linked logins at this time, please extend this method to look thru all current UserCredentials link to other BasicProfiles-passwordInfo
    /*
    Future.successful {
      for (
        found <- users.values.find(_ == user);
        identityWithPasswordInfo <- found.identities.find(_.providerId == UsernamePasswordProvider.UsernamePassword)
      ) yield {
        identityWithPasswordInfo.passwordInfo.get
      }
    }
    */
    Future.successful{user.passwordInfo()}
  }

  def find(providerId: String, userId: String): Future[Option[BasicProfile]] = {
    /* We don't support linked logins at this time, to enable parse thru all linked logins for all the found users
    if (play.api.Logger.isDebugEnabled) {
      play.api.Logger.debug("users = %s".format(users))
    }
    val result = for (
      user <- users.values;
      basicProfile <- user.identities.find(su => su.providerId == providerId && su.userId == userId)
    ) yield {
      basicProfile
    }
    Future.successful(result.headOption)
    */

    val u = findByUserIdAndProviderId(userId, providerId) match {
      case null => None
      case uc => Some(userCredentialService.userCredential2basicProfile(uc))
    }
    Future.successful(u)
  }

  def updatePasswordInfo(user: UserCredential, info: securesocial.core.PasswordInfo): Future[Option[BasicProfile]] = {
    // We don't support linked logins at this time, please extend this method to look thru all current UserCredentials link to other BasicProfiles-passwordInfo
    /*
    Future.successful {
      for (
        found <- users.values.find(_ == user);
        identityWithPasswordInfo <- found.identities.find(_.providerId == UsernamePasswordProvider.UsernamePassword)
      ) yield {
        val idx = found.identities.indexOf(identityWithPasswordInfo)
        val updated = identityWithPasswordInfo.copy(passwordInfo = Some(info))
        val updatedIdentities = found.identities.patch(idx, Seq(updated), 1)
        found.copy(identities = updatedIdentities)
        updated
      }
    }
    */
    val userCopy: BasicProfile = userCredentialService.userCredential2basicProfile(user)
    userCopy.copy(passwordInfo = Some(info))
    Future.successful{Some(userCopy)}

  }

  def findByEmailAndProvider(email: String, providerId: String): Future[Option[securesocial.core.BasicProfile]] = {
    /* No linked support yet, to enable, as per normal loop thru all existing linked logins
    if (logger.isDebugEnabled) {
      logger.debug("users = %s".format(users))
    }
    val someEmail = Some(email)
    val result = for (
      user <- users.values;
      basicProfile <- user.identities.find(su => su.providerId == providerId && su.email == someEmail)
    ) yield {
      basicProfile
    }
     */

    //val log : Long = customUtils.Helpers.startPerfLog()

    val retUser: Option[BasicProfile] = getUserByEmailAndProvider(email.toLowerCase, providerId) match {
      case None => None
      case Some(user) => Some( userCredentialService.userCredential2basicProfile(user))
    }

    Future.successful(retUser)

    //customUtils.Helpers.endPerfLog("findByEmailAndProvider - find with id: " + email, log)
  }




  /** *
    * Store Identity in the database
    * @param user
    * @return
    */
  def save(user: BasicProfile, mode: securesocial.core.services.SaveMode ): Future[UserCredential] = {
    // We don't support linked logins at this time, please extend this method to look thru all current UserCredentials link to other BasicProfiles-passwordInfo
    /*
    mode match {
          case SaveMode.SignUp =>
            val newUser = DemoUser(user, List(user))
            users = users + ((user.providerId, user.userId) -> newUser)
          case SaveMode.LoggedIn =>

        }
        // first see if there is a user with this BasicProfile already.
        val maybeUser = users.find {
          case (key, value) if value.identities.exists(su => su.providerId == user.providerId && su.userId == user.userId) => true
          case _ => false
        }
        maybeUser match {
          case Some(existingUser) =>
            val identities = existingUser._2.identities
            val updatedList = identities.patch(identities.indexWhere(i => i.providerId == user.providerId && i.userId == user.userId), Seq(user), 1)
            val updatedUser = existingUser._2.copy(identities = updatedList)
            users = users + (existingUser._1 -> updatedUser)
            Future.successful(updatedUser)

          case None =>
            val newUser = DemoUser(user, List(user))
            users = users + ((user.providerId, user.userId) -> newUser)
            Future.successful(newUser)
        }
     */

    //var log : Long = customUtils.Helpers.startPerfLog()
    val userCredential : UserCredential = userCredentialService.basicProfile2UserCredential(user)
    val userCredential2 = createOrUpdateUser(userCredential)
    //customUtils.Helpers.endPerfLog("save - find with id: " + user.email, log)

    Future.successful(userCredential2)
  }




  /**
   * Checks if Identity exist meaning UserId and ProviderId
   * @param userId
   * @param providerId
   * @return id in Neo4j database and if the user exits true else false
   */

  @Transactional(readOnly = true)
  def exists(userId: String, providerId: String) :  (UUID, Boolean, Long) = {

    //var log : Long = customUtils.Helpers.startPerfLog()

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

    //customUtils.Helpers.endPerfLog("exists - find with id: " + userId, log)

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
  def findByUserIdAndProviderId(userId: String, providerId: String) : UserCredential =  {

    val mUserId : String = userId.toLowerCase

  // search in user cache
    if(isUserCacheON){
      val u = findUserCache(userId, providerId)

      if(u != null && u != None && u.get != null && u.get != None) {
        return u.get
      }
    }

    val user = userCredentialService.userCredentialRepository.findByuserIdAndProviderId(mUserId,providerId)

    if(user != null && user != None && isUserCacheON) {
      addUserCache(user)
    }

    return user
  }

  // Fetch user by emailAddress and providerId
  @Transactional(readOnly = true)
  def getUserByEmailAndProvider(emailAddress: String, providerId: String) : Option[UserCredential] = {
    userCredentialService.userCredentialRepository.findByemailAddressAndProviderIdToLower(emailAddress.toLowerCase, providerId) match {
      case null => None
      case item:UserCredential => Some(item)
    }
  }


  // List alla users
  @Transactional(readOnly = true)
  def getUsers() :  List[UserCredential] =  {
    IteratorUtil.asCollection(userCredentialService.userCredentialRepository.findAll()).asScala.toList
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

    //var log : Long = customUtils.Helpers.startPerfLog()

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

        val theProfile = modUserCredential.getUserProfile
        var saveProfile : Boolean = false

        Logger.info("# Viewed By Member ...")

        if(theProfile.getmemberVisited() == null || theProfile.getmemberVisited() == None) {
          Logger.info("Create ViewedByMember")
          val view: models.ViewedByMember = new models.ViewedByMember()
          Logger.info("ViewedByMember size: " + view.getSize)

          userProfileService.setAndRemoveViewByMember(theProfile, view)
          saveProfile = true
        }

        // At ViewedByUnKnown
        if(theProfile.getUnKnownVisited() == null || theProfile.getUnKnownVisited == None) {
          Logger.info("Create ViewedByUnKnown")
          val view: models.ViewedByUnKnown = new ViewedByUnKnown()
          userProfileService.setAndRemoveViewByUnKnown(theProfile, view)
          saveProfile = true
        }

        if(saveProfile) {
          Logger.info("Save profile")
          userProfileService.saveUserProfile(theProfile)
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
        modUserCredential.lastName                = userCredential.lastName
        modUserCredential.fullName                = userCredential.fullName
        modUserCredential.salt                    = userCredential.salt
        modUserCredential.hasher                  = userCredential.hasher

     // personnummer is not given here
     // modUserCredential.personNummer            = userCredential.personNummer

        var newUserCredential                   = saveUser(modUserCredential)

      //customUtils.Helpers.endPerfLog("createOrUpdateUser - update", log)
        return newUserCredential

    } else {
        // Add default group
        userCredentialService.addRole(userCredential, RoleEnums.USER)
        var userProfile : UserProfile = new UserProfile()

      userProfile.userIdentity = userCredential.userId
      userProfile.providerIdentity = userCredential.providerId
      //userProfile.firstName = userCredential.firstName
      //userProfile.lastName = userCredential.lastName
      userProfile.keyIdentity = userProfile.userIdentity + "_" + userProfile.providerIdentity

      userCredential.userId = userId // lowercase
      userCredential.emailAddress = userCredential.emailAddress.toLowerCase // lowcase

      var storedUserProfile = userProfileService.saveUserProfile(userProfile)
      userCredentialService.addUserProfile(userCredential, storedUserProfile)
      var newUserCredential = saveUser(userCredential)


      //customUtils.Helpers.endPerfLog("createOrUpdateUser insert", log)
        return newUserCredential
    }


  }

  @Transactional(readOnly = false)
  def addRole(userCredential: UserCredential, role: RoleEnums): UserCredential = {

    //var log : Long = customUtils.Helpers.startPerfLog()

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

      userCredentialService.addRole(userCredential, role)

      var newUserCredential                   = saveUser(modUserCredential)

      //customUtils.Helpers.endPerfLog("addRole", log)
      return newUserCredential

    } else {
      // Add default group
      userCredentialService.addRole(userCredential, role)
      var newUserCredential = saveUser(userCredential)
      //customUtils.Helpers.endPerfLog("addRole", log)
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
    val modUser = userCredentialService.userCredentialRepository.save(userCredential)
    modUser
  }








  // Tokens
/*
  def save(token: MailToken) {
    tokens += (token.uuid -> token)
  }
*/

  def saveToken(token: MailToken): Future[MailToken] = {
    Future.successful {
      tokens += (token.uuid -> token)
      token
    }
  }

  def findToken(token: String): Future[Option[MailToken]] = {
    Future.successful { tokens.get(token) }
  }

  def deleteToken(uuid: String): Future[Option[MailToken]] = {
    Future.successful {
      tokens.get(uuid) match {
        case Some(token) =>
          tokens -= uuid
          Some(token)
        case None => None
      }
    }
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
