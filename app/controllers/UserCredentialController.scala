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
import securesocial.core.providers.utils.BCryptPasswordHasher
import play.core.SourceMapper
import play.api.Mode.Mode
import java.io.File
import securesocial.core.PasswordInfo

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
/*
  @Autowired
  var userCredentialService: UserCredentialService = _
*/

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
    userCredential.lastName = "lastname"
    //userCredential.oAuth1InfoSecret = "dfdsf"
    userCredential.providerId = "facebook"


   // userCredentialService.saveUser(userCredential)
    Ok("OK")
  }

  def visa = Action {
    /*
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



    println("Name: Fabian")

    val element5 = userCredentialService.getUserById("fabianfacebook","facebook")
    println("Userid : " + element5.userId)
    println("providerId : " + element5.providerId)
    println("Id : " + element5.id)
    println("FirstName : " + element5.firstName)
    println("LastName : " + element5.lastName)
    var l = element5.lastName
    if(l.equals("")) {
      l = "Svensson"
    }
    element5.lastName = element5.firstName
    element5.firstName = l

    val upd5 = userCredentialService.createOrUpdateUser(element5)
    println("Userid : " + upd5.userId)
    println("providerId : " + upd5.providerId)
    println("Id : " + upd5.id)
    println("FirstName : " + upd5.firstName)
    println("LastName : " + upd5.lastName)

    var el6 : UserCredential = new UserCredential()
    el6.userId = "test"
    el6.providerId = "test"
    el6.firstName = "Elmaco"
    el6.lastName = "MacDonalds"
    el6.emailAddress = "el@mac.com"
    val upd6 = userCredentialService.createOrUpdateUser(el6)


    val finns1 = userCredentialService.exists("fabianfacebook","facebook")
    println("Finns : " + finns1._2)
    println("Id : " + finns1._1)






    val element6 = userCredentialService.getUserById("no","facebook")
    println("no : " + element6.userId)
    println("key: " + element6.id)






    val fins2 = userCredentialService.exists("no","facebook")
    println("Finns2 : " + fins2._2)
    println("Id2: " + fins2._1)

  */

    var app : Application = new Application {override def path: File = ???

      override def plugins: Seq[Plugin] = ???

      override def configuration: Configuration = ???

      override def mode: Mode = ???

      override def classloader: ClassLoader = ???

      override def global: GlobalSettings = ???

      override def sources: Option[SourceMapper] = sources
    }
    var b : BCryptPasswordHasher = new BCryptPasswordHasher(app)
    var p: PasswordInfo  = b.hash("sommar14")

    Ok(p.password)
  }
}