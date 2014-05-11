package services

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


/**
 * UserCredentialService
 * Methods for manages SecureSocial storage in Neo4J graphic database
 *
 * 1. Username/Password storage
 * 2. Facebook autentication
 *
 */


@Service
class UserCredentialService {


  @Autowired
  private var template: Neo4jTemplate = _

  @Autowired
  private var userCredentialRepository: UserCredentialRepository = _




  def findByEmailAndProvider(email: String, providerId: String): Option[Identity] = {
    var identity : Identity = new Identity {
      override def firstName: String = ???

      override def identityId: IdentityId = ???

      override def email: Option[String] = ???

      override def authMethod: AuthenticationMethod = ???

      override def passwordInfo: Option[PasswordInfo] = ???

      override def avatarUrl: Option[String] = ???

      override def oAuth2Info: Option[OAuth2Info] = ???

      override def oAuth1Info: Option[OAuth1Info] = ???

      override def fullName: String = ???

      override def lastName: String = ???
    }

    Option(identity)
  }


// Kontrollera om objekt finns dvs id annars sätt inte id
// skall användas vid spara
  def find(id: IdentityId): Option[Identity] = {

    var identity : Identity = new Identity {
      override def firstName: String = ???

      override def identityId: IdentityId = ???

      override def email: Option[String] = ???

      override def authMethod: AuthenticationMethod = ???

      override def passwordInfo: Option[PasswordInfo] = ???

      override def avatarUrl: Option[String] = ???

      override def oAuth2Info: Option[OAuth2Info] = ???

      override def oAuth1Info: Option[OAuth1Info] = ???

      override def fullName: String = ???

      override def lastName: String = ???
    }

    //identity.oAuth1Info


    Option(identity)
  }


  // Kontrollera om elemen redan finns eller inte
  //  IdentityId = userId + providerId
  // om element redan finns skall id med dvs sättas.
  def save(user: Identity): Identity = {

    // check if exists
    var uc  : UserCredential = getUserById(user.identityId.userId, user.identityId.providerId)
    //var uc2 : UserCredential = socialUser2UserCredential(user)
    // Ett värde som säger om värdet redan finns i databasen uc.id


    user
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
    var userCredentialIndex: Index[Node] = template.getIndex(classOf[UserCredential],"userId")
    var hits = userCredentialIndex.query("userId", userId)

    var nods = hits.iterator()
    var id : Long = 0
    while(nods.hasNext) {
      id = nods.next().getId
      var node : UserCredential = new UserCredential()
      node = userCredentialRepository.findOne(id)

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
    var userCredentialIndex: Index[Node] = template.getIndex(classOf[UserCredential], "emailAddress")
    var hits = userCredentialIndex.query("emailAddress", emailAddress)

    var nods = hits.iterator()
    var id : Long = 0
    while(nods.hasNext) {
      id = nods.next().getId
      var node : UserCredential = new UserCredential()
      node = userCredentialRepository.findOne(id)

      list += node
    }

    list.toList
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
    var modUser =  userCredentialRepository.save(userCredential)

    modUser
  }



  def socialUser2UserCredential(socialUser:Identity ) {
    var userCredential : UserCredential = new UserCredential()
    val oauth2: OAuth2Info = socialUser.oAuth2Info.getOrElse(new OAuth2Info(""))
    val oauth1a: OAuth1Info = socialUser.oAuth1Info.getOrElse(new OAuth1Info("",""))
    val oauth1b: OAuth1Info = socialUser.oAuth1Info.getOrElse(new OAuth1Info("",""))


    userCredential.providerId         = socialUser.identityId.providerId
    userCredential.userId             = socialUser.identityId.userId
    userCredential.firstName          = socialUser.firstName
    userCredential.lastName           = socialUser.lastName
    userCredential.fullName           = socialUser.fullName
    //userCredential.authMethod       = socialUser.authMethod
    userCredential.oAuth1InfoToken  = socialUser.oAuth1Info.getOrElse(oauth1a).token
    userCredential.oAuth1InfoSecret = socialUser.oAuth1Info.getOrElse(oauth1b).secret

    userCredential.oAuth2InfoAccessToken = oauth2.accessToken
    userCredential.oAuth2InfoExpiresIn = oauth2.expiresIn.getOrElse(0).toString
    userCredential.oAuth2InfoRefreshToken = oauth2.refreshToken.getOrElse("") // Option
    userCredential.oAuth2InfoTokenType = oauth2.tokenType.getOrElse("") // Option

    userCredential.avatarUrl = socialUser.avatarUrl.getOrElse("")
    userCredential.emailAddress = socialUser.email.getOrElse("")


    userCredential
  }



  // IdentityId = userId + providerId

  def userCredential2socialUser(userCredential: UserCredential): Identity = {
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
      )),
      oAuth2Info = Some(new OAuth2Info(
                    userCredential.oAuth2InfoAccessToken,
                   Some(userCredential.oAuth2InfoTokenType),
                   Some(userCredential.oAuth2InfoExpiresIn.toInt),
                   Some(userCredential.oAuth2InfoRefreshToken))),
      passwordInfo = Some(
                    new PasswordInfo(
                      userCredential.hasher,
                      userCredential.password,
                      Some(userCredential.salt) ))
                    )
  }


}
