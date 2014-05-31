package services

import play.api.{Logger, Application}
import securesocial.core._
import securesocial.core.providers.Token
import securesocial.core.IdentityId
import org.springframework.stereotype.Service
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.neo4j.support.Neo4jTemplate
import repositories.UserProfileRepository


@Service
object OneMemoryClassUserService {

}


class OneMemoryClassUserService(application: Application) extends UserServicePlugin(application) {


  val logger = Logger("application.controllers.OneMemoryClassUserService")
  // a simple User class that can have multiple identities
  case class User(id: String, identities: List[Identity])

  //
  var users = Map[String, User]()
  //private var identities = Map[String, Identity]()
  private var tokens = Map[String, Token]()

  var user_identityId_providerId  = ""
  var user_identityId_userId      = ""
  var user_firstName              = ""
  var user_lastName               = ""
  var user_fullName               = ""
  var user_authMethod             = ""
  var user_oAuth1Info_token       = ""
  var user_oAuth1Info_secret      = ""
  var oauth2_accessToken          = ""
  var oauth2_expiresIn            = ""
  var oauth2_refreshToken         = ""
  var oauth2_tokenType            = ""
  var user_avatarUrl              = ""
  var user_email                  = ""
  var pinfo_hasher                = ""
  var pinfo_password              = ""
  var pinfo_salt                  = ""



  def find(id: IdentityId): Option[Identity] = {
    if ( logger.isDebugEnabled ) {
      logger.debug("users = %s".format(users))
    }


    val result = for (
      user <- users.values ;
      identity <- user.identities.find(_.identityId == id)
    ) yield {
      identity
    }

    if(result.size > 0) {
      var it = result.head
      visa(it, "find")
    }




    result.headOption
  }




  def findByEmailAndProvider(email: String, providerId: String): Option[Identity] = {
    if ( logger.isDebugEnabled ) {
      logger.debug("findByEmailAndProvider( : " + email + ", " + providerId + ") ")
 //     logger.debug("users = %s".format(users))
    }


    val result = for (
      user <- users.values ;
      identity <- user.identities.find(i => i.identityId.providerId == providerId && i.email.exists(_ == email))
    ) yield {
      identity
    }

    if(result.size > 0) {
      var it = result.head
      visa(it, "findByEmailAndProvider")
    }


    result.headOption
  }




  def visa(user: Identity, caller: String) {

    println("-------------------------------------------------------------------------------")
    println("METOD : " + caller)
    println("-------------------------------------------------------------------------------")
    printf("\nuserId      : %s", user.identityId.userId)
    printf("\nproviderId  : %s", user.identityId.providerId)
    printf("\nfirstName   : %s", user.firstName)
    printf("\nlastName    : %s", user.lastName)
    printf("\nfulName     : %s", user.fullName)
    printf("\nauthMethode : %s", user.authMethod.method)
    printf("\nauthMethode : %s", user.passwordInfo.get.password)
    printf("\nhasher      : %s", user.passwordInfo.get.hasher)
    printf("\nsalt        : %s", user.passwordInfo.get.salt)
    printf("\nemail       : %s", user.email)

    printf("\noauth2_accessToken        : %s", user.oAuth2Info.getOrElse(""))
    printf("\noauth2_expiresIn          : %s", user.oAuth2Info.getOrElse(""))
    printf("\noauth2_refreshToken       : %s", user.oAuth2Info.getOrElse(""))
    printf("\noauth2_tokenType          : %s", user.oAuth2Info.getOrElse(""))

    printf("\nuser_oAuth1Info_token       : %s", user.oAuth1Info.getOrElse(""))
    printf("\nuser_oAuth1Info_secret      : %s", user.oAuth1Info.getOrElse(""))

    printf("\nuser_avatarUrl       : %s", user.avatarUrl)
    println("-------------------------------------------------------------------------------")
  }




  def save(user: Identity): Identity = {
    // first see if there is a user with this Identity already.
    val maybeUser = users.find {
      case (key, value) if value.identities.exists(_.identityId == user.identityId ) => true
      case _ => false
    }


    visa(user, "save")



    maybeUser match {
      case Some(existingUser) =>
        val identities = existingUser._2.identities
        val updated = identities.patch( identities.indexWhere( i => i.identityId == user.identityId ), Seq(user), 1)
        users = users + (existingUser._1 -> User(existingUser._1, updated))
      case _ =>
        val newId = System.currentTimeMillis().toString
        users = users + (newId -> User(newId, List(user)))
    }
    // this sample returns the same user object, but you could return an instance of your own class
    // here as long as it implements the Identity trait. This will allow you to use your own class in the protected
    // actions and event callbacks. The same goes for the find(id: IdentityId) method.
    user
  }





  def link(current: Identity, to: Identity) {
    val currentId = current.identityId.userId + "-" + current.identityId.providerId
    val toId = to.identityId.userId + "-" + to.identityId.providerId
    Logger.info(s"linking $currentId to $toId")

    val maybeUser = users.find {
      case (key, value) if value.identities.exists(_.identityId == current.identityId ) => true
    }

    maybeUser.foreach { u =>
      if ( !u._2.identities.exists(_.identityId == to.identityId)) {
        // do the link only if it's not linked already
        users = users + (u._1 -> User(u._1, to :: u._2.identities  ))
      }
    }
  }

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
