package services

// MATCH (tom) RETURN tom
// MATCH (tom) DELETE tom



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
 *
 */


@Service
object UserCredentialService {

  @Autowired
  private var template: Neo4jTemplate = _

  @Autowired
  private var userCredentialRepository: UserCredentialRepository = _

}


class UserCredentialService  (application: Application) extends UserServicePlugin(application)
 {

  private var tokens = Map[String, Token]()

  var users = Map[String, Identity]()



  // Kontrollerar om id finns
  // dvs. userid och provider id genom att söka i databasen.

  def find(id: IdentityId): Option[Identity] = {
    println("find ")
    println("----------------------------------------------------------------------")
    println("Arguments:")
    println("----------------------------------------------------------------------")
    println("ProviderId : " + id.providerId)
    println("userId     : " + id.userId)
    println("----------------------------------------------------------------------")

    val exitsUser = exists(id.userId, id.providerId)
    var uc  : UserCredential =  getuser(id.userId, id.providerId)


    println("############################################################")
    var b : BCryptPasswordHasher = new BCryptPasswordHasher(application)
    var p: PasswordInfo  = b.hash("sommar14")
    println("Password: " + p.password)
    println("hasheter: " + p.hasher)

    println("password check : " + b.matches(p, "sommar14"))

    println("############################################################")
    var p2 : PasswordInfo = new PasswordInfo("bcrypt","$2a$10$3GWlC1dXKYHh.v9swFHcQuoNBtILkrqxZ2Pm7SmYUQikzABKkHsnW", Some("test"))
    println("password check2 : " + b.matches(p2, "sommar14"))
    println("############################################################")




    println("ID   : " + exitsUser._1)
    println("FINNS: " + exitsUser._2)


    if(exitsUser._2 == true){

      println("FirstName : " + uc.firstName )
      println("LastName : " + uc.lastName )
      println(".........................................................................")
      println("find:....................................................................")
      println(".........................................................................")
      println(userCredential2socialUser(uc))
      println(".........................................................................")
      userCredential2socialUser(uc)
    }

    None
  }



  // find by email and provider
  def findByEmailAndProvider(email: String, providerId: String): Option[Identity] = {

    println("Method: findByEmailAndProvider  ...................")
    println("UserId: " + email)
    println("UserProvider: " + providerId)

    var uc  : UserCredential =  getuser(email, providerId)

    val exitsUser = exists(email, providerId)

    println("fetched userid : " + uc.userId)
    println("fetched userid : " + uc.firstName)
    println("fetched userid : " + uc.lastName)


    //uc.emailAddress = email


    if(exitsUser._2 == true){

      println(".........................................................................")
      println("findByEmailAndProvider:..................................................")
      println(".........................................................................")
      println(userCredential2socialUser(uc))
      println(".........................................................................")
      userCredential2socialUser(uc)

    }
    None

  }





  // Kontrollera om elemen redan finns eller inte
  //  IdentityId = userId + providerId
  // om element redan finns skall id med dvs sättas.
  def save(user: Identity): Identity = {

    println("Method: Save ...................")
    println("UserId:        " + user.identityId.userId)
    println("UserProvider:  " + user.identityId.providerId)
    println("email:         " + user.email)
    println("First name : " + user.firstName)


    println("----------------------------------------------------------------------")
    println("USER - INDATA ")
    println("----------------------------------------------------------------------")
    println("PROVIDER: " + user.identityId.providerId)
    println("USERID: " + user.identityId.userId)
    println("FIRSTNAME : " + user.firstName)
    println("LASTNAME : " + user.lastName)
    println("FULNAME : " + user.fullName)
    println("EMAIL : " + user.email)
    println("PASSWORD: " + user.passwordInfo.get.password)
    println("SALT: " + user.passwordInfo.get.salt)
    println("HASHER: " + user.passwordInfo.get.hasher)
    println("authMethod: " + user.authMethod)
    println("AVATAR_URL : " + user.avatarUrl)

    println("----------------------------------------------------------------------")

    // Konvertera in data för att kunna spara ner i databasen
    // spara inte nere i databasen utan kovertera tillbaka
    var user2 : Identity = userCredential2socialUser(socialUser2UserCredential(user))

    println("----------------------------------------------------------------------")
    println("USER2 - KONTROLL ")
    println("----------------------------------------------------------------------")

    println("PROVIDER: " + user2.identityId.providerId)
    println("USERID: " + user2.identityId.userId)
    println("FIRSTNAME : " + user2.firstName)
    println("LASTNAME : " + user2.lastName)
    println("FULNAME : " + user2.fullName)
    println("EMAIL : " + user2.email)
    println("PASSWORD: " + user2.passwordInfo.get.password)
    println("SALT: " + user2.passwordInfo.get.salt)
    println("HASHER: " + user2.passwordInfo.get.hasher)
    println("authMethod: " + user2.authMethod)
    println("AVATAR_URL : " + user2.avatarUrl)
    println("----------------------------------------------------------------------")


    if(user.passwordInfo == user2.passwordInfo) {
      println("passwordInfo is ok")
    } else {
      println("passwordInfo error!!!")
    }

    if(user.oAuth1Info == user2.oAuth1Info) {
      println("oAuth1Info : OK")
    } else {
      println("oAuth1Info : ERROR")
    }

    if(user.oAuth2Info == user2.oAuth2Info) {
      println("oAuth2Info : OK")
    } else {
      println("oAuth2Info : ERROR")
    }

   if(user == user2) {
      println("User is ok")
    } else {
      println("User error!!")
    }

    // Kontrollera om användaren finns
    val exitsUser = exists(user.identityId.userId, user.identityId.providerId)

    println("Finns: " + exitsUser)


    // check if exists
    var uc  : UserCredential = getUserById(user.identityId.userId, user.identityId.providerId)

    println("id: " + uc.id)


    var userCredential : UserCredential = socialUser2UserCredential(user)
    if(exitsUser._2 == true) {
      println("user finns")
    }

    var userCredential2 = createOrUpdateUser(userCredential)

    println("svar: " + userCredential2.firstName )

    //var uc2 : UserCredential = socialUser2UserCredential(user)
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



  def socialUser2UserCredential(socialUser:Identity ): UserCredential = {

    var userCredential : UserCredential = new UserCredential()

    val oauth2:   OAuth2Info    = socialUser.oAuth2Info.getOrElse(new OAuth2Info(""))
    var pinfo:    PasswordInfo  = socialUser.passwordInfo.getOrElse( new PasswordInfo("","",Some("")) )

    userCredential.providerId         = socialUser.identityId.providerId   // name
    userCredential.userId             = socialUser.identityId.userId       // userid
    userCredential.firstName          = socialUser.firstName
    userCredential.lastName           = socialUser.lastName
    userCredential.fullName           = socialUser.fullName
    userCredential.authMethod       = socialUser.authMethod.toString // ???? Kontrollera  ????????

    // Password information
    userCredential.password = pinfo.password
    userCredential.hasher  = pinfo.hasher
    userCredential.salt    = pinfo.salt.getOrElse("")

    // oAuth1
    userCredential.oAuth1InfoToken  = socialUser.oAuth1Info.getOrElse(new OAuth1Info("","")).token
    userCredential.oAuth1InfoSecret = socialUser.oAuth1Info.getOrElse(new OAuth1Info("","")).secret

    // oAuth2
    userCredential.oAuth2InfoAccessToken    = oauth2.accessToken
    userCredential.oAuth2InfoExpiresIn      = oauth2.expiresIn.getOrElse(0).toString
    userCredential.oAuth2InfoRefreshToken   = oauth2.refreshToken.toString
    userCredential.oAuth2InfoTokenType      = oauth2.tokenType.toString

    userCredential.avatarUrl = socialUser.avatarUrl.getOrElse("")
    userCredential.emailAddress = socialUser.email.getOrElse("")


    userCredential
  }





  // Från databasen till SecureSocial
  // IdentityId = userId + providerId
  //
  def userCredential2socialUser(userCredential: UserCredential): Identity = {

    var oAuth2InfoExpiresIn : Int = 0
    var salt : String  = ""

    if(userCredential.oAuth2InfoExpiresIn.equals("")) {
      oAuth2InfoExpiresIn = 0
    } else {
      oAuth2InfoExpiresIn = userCredential.oAuth2InfoExpiresIn.toInt
    }

    println("password: " + userCredential.password)
    println("salt    : " + userCredential.salt)
    println("hasher  : " + userCredential.hasher)


    val returv =
    new SocialUser(
      identityId=new IdentityId(
        userId = userCredential.userId,
        providerId = userCredential.providerId
      ),
      firstName=userCredential.firstName,
      lastName=userCredential.lastName,
      fullName=userCredential.fullName,
      email=Some(userCredential.emailAddress),
      avatarUrl= Some(userCredential.avatarUrl),
      authMethod= new AuthenticationMethod(
        userCredential.authMethod
      ),


      oAuth1Info = Some(new OAuth1Info(
         userCredential.oAuth1InfoToken,
        userCredential.oAuth1InfoSecret
      ))
      ,
      oAuth2Info = Some(new OAuth2Info(
                    userCredential.oAuth2InfoAccessToken,
                   Some(userCredential.oAuth2InfoTokenType),
                   Some(oAuth2InfoExpiresIn),
                   Some(userCredential.oAuth2InfoRefreshToken)))

                   ,
      passwordInfo = Some(
                    new PasswordInfo(
                      userCredential.hasher,
                      userCredential.password,
                      Some(userCredential.salt))
                    ))

    //new PasswordInfo("","")


    println("METOD: " + returv.authMethod.method)
    println("METOD_: " + returv.authMethod.productArity)


    returv
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
