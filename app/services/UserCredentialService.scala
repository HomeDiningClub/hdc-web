package services

import org.springframework.stereotype.Service
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.neo4j.support.Neo4jTemplate
import repositories.UserCredentialRepository
import models.UserCredential
import securesocial.core._
import scala.Some
import scala.Some
import securesocial.core.OAuth2Info
import securesocial.core.OAuth1Info
import securesocial.core.IdentityId
import scala.Some

@Service
object UserCredentialService {

  @Autowired
  var template: Neo4jTemplate = _

  @Autowired
  var userCredentialRepository: UserCredentialRepository = _

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

  def socialUser2UserCredential(socialUser:Identity ): UserCredential = {

    var userCredential : UserCredential = new UserCredential()

    val oauth2:   OAuth2Info    = socialUser.oAuth2Info.getOrElse(new OAuth2Info(""))
    var pinfo:    PasswordInfo  = socialUser.passwordInfo.getOrElse( new PasswordInfo("","",Some("")) )

    userCredential.providerId         = socialUser.identityId.providerId   // name
    userCredential.userId             = socialUser.identityId.userId       // userid
    userCredential.firstName          = socialUser.firstName
    userCredential.lastName           = socialUser.lastName
    userCredential.fullName           = socialUser.fullName
    //userCredential.authMethod       = socialUser.authMethod.toString // ???? Kontrollera  ????????

    // todo bättre kontroll att det finns ett värde
    if(socialUser.authMethod.toString.size > 0) {
      userCredential.authMethod = socialUser.authMethod.method
    } else {
      userCredential.authMethod = ""
    }


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

    //userCredential.oAuth2InfoRefreshToken   = oauth2.refreshToken.toString
    userCredential.oAuth2InfoRefreshToken   = oauth2.refreshToken.getOrElse("")
    userCredential.oAuth2InfoTokenType      = oauth2.tokenType.getOrElse("")

    userCredential.avatarUrl = socialUser.avatarUrl.getOrElse("")
    userCredential.emailAddress = socialUser.email.getOrElse("")


    userCredential
  }

}

@Service
class UserCredentialService {

    @Autowired
    var template: Neo4jTemplate = _

    @Autowired
    var userCredentialRepository: UserCredentialRepository = _



}
