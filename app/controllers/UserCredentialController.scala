package controllers

import org.springframework.beans.factory.annotation.Autowired
import play.api._
import play.api.mvc._
import services.{UserCredentialService, UserProfileService}
import play.data.Form
import models.UserProfileData
import org.springframework.stereotype.{Controller => SpringController}
import play.api.data.Form
import play.data.Form

import play.api.data._
import play.api.data.Forms._
import models.UserCredential

/*
*
* [info] application - [securesocial] unloaded identity provider: instagram
* [info] application - [securesocial] unloaded identity provider: vk
* [info] application - [securesocial] unloaded identity provider: xing
* [info] application - [securesocial] unloaded event listener my_event_listener
* [info] application - [securesocial] unloaded identity provider: github
* [info] application - [securesocial] unloaded identity provider: userpass
* [info] application - [securesocial] unloaded identity provider: linkedin
* [info] application - [securesocial] unloaded identity provider: google
* [info] application - [securesocial] unloaded identity provider: facebook
* [info] application - [securesocial] unloaded identity provider: twitter
* [info] application - [securesocial] unloaded password hasher bcrypt
* [info] play - Shutdown application default Akka system.
*
*
* */



@SpringController
class UserCredentialController extends Controller {

  @Autowired
  var userCredentialService: UserCredentialService = _


  def spara = Action {
    var userCredential: UserCredential = new UserCredential()

    //userCredential.id = 0L
    //userCredential.authMethod = "a"
    // userCredential.avatarUrl  = "b"
    userCredential.emailAddress = "fabian@gmail.com"
    userCredential.userId = "fabianfacebook"
    userCredential.firstName = "Fabian"
    //userCredential.fullName = "test test"
    //userCredential.hasher = "test"
    //userCredential.lastName = "lastname"
    //userCredential.oAuth1InfoSecret = "dfdsf"
    userCredential.providerId = "facebook"


    userCredentialService.saveUser(userCredential)
    Ok("OK")
  }

  def visa = Action {
    val list = userCredentialService.getUser("fabian@gmail.com")

    for( v <- list) {
      println("EMAIL : " + v.emailAddress)
      println("FIRSTNAME : " + v.firstName)
      println("LASTNAME : " + v.lastName)
      println("USERID : " + v.userId)
      println("PROVIDER : " + v.providerId)
    }


    val element = userCredentialService.getuser("fabian@gmail.com", "facebook")
    println("facebook [YY]: " + element.firstName)

    val element2 = userCredentialService.getuser("fabian@gmail.com", "password")
    println("password [YY]: " + element2.firstName)

    val element3 = userCredentialService.getuser("fabian@gmail.com", "gmail")
    println("gmail [NN]: " + element3.firstName)

    val element4 = userCredentialService.getuser("fabian@gmail.com", "test")
    println("test [YN] : " + element4.firstName)


    val element5 = userCredentialService.getUserById("fabianfacebook","facebook")
    println("test 5 [YN] : " + element5.firstName)

    Ok("Visa")
  }
}