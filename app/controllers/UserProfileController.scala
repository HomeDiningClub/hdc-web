package controllers

// http://www.playframework.com/documentation/2.2.x/ScalaForms

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.{Controller => SpringController}

import services.UserProfileService
import models.UserProfileData
import models.formdata.UserProfile


import play.api.data._
import play.api.data.Forms._

import play.api._
import play.api.mvc._


@SpringController
class UserProfileController  extends Controller{

  @Autowired
  var userProfileService: UserProfileService = _






  val userProfileForm : play.api.data.Form[UserProfile]  = play.api.data.Form(
    mapping(
      "userName" -> nonEmptyText,
      "emailAddress" -> email,
      "firstName" -> text,
      "lastName" -> text,
      "aboutme" -> text,
      "quality" -> list(boolean),
      "idno" -> longNumber
    )(UserProfile.apply)(UserProfile.unapply)
  )




    val AnvandareForm = Form(
      mapping(
        "name" -> text,
        "emails" -> list(text),
        "quality" -> list(text)
      )
      (EnvData.apply) (EnvData.unapply)
    )


def skapavy = Action {

  // Pre selected
  val typ = new models.Types
  typ.addVald("Amerikanskt")
  typ.addVald("LCHF")
  typ.addVald("Budget")
  typ.addVald("Finns_inte")
  typ.addVald("Kött")

  val eData : EnvData = new controllers.EnvData("user", List("adam","bertil", "cesar"), List("adam", "bertil"))
  val nyForm =  AnvandareForm.fill(eData)
 Ok(views.html.profile.skapa(nyForm, typ.findAll, typ))
}


def taemot = Action {
    implicit request =>
      AnvandareForm.bindFromRequest.fold(
        errors => {
          if(errors.hasErrors) {
            println("Fel data!")

            println("Fel lista: "  + errors.toString)
          }

          // Felaktigt ifyllt formulär

        },
        anvadare => {
            // test
            println(anvadare.name)
            for(v <- anvadare.emails) {
                println("v : " + v)
            }

            println("Do" + anvadare.quality.size)
            for(d <- anvadare.quality) {
                println("v : " + d)
            }


        })
    Ok("OK")
}



  def login = Action {
    //Redirect(routes.Application.hello("Bob"))
    // Redirect(routes.securesocial.controllers.LoginPage.login)
    Redirect("/login",301)
    //routes.securesocial.controllers.LoginPage.login
    // securesocial.controllers.LoginPage.login
    //Ok("OK")
  }


  def index = Action {
    val userProfilesList = userProfileService.getAllUserProfiles()
    Ok(views.html.profile.listofProfiles(userProfilesList))
  }


  /*
  def form2Data(userProfile: UserProfile) : UserProfileData {


    userProfileData
  }
*/


  def saveUserProfile = Action {
    implicit request =>
      userProfileForm.bindFromRequest.fold(
        errors => {

          if(errors.hasErrors) {
            println("Fel data!")

            println("Fel lista: "  + errors.toString)


          }

          // Felaktigt ifyllt formulär
          Ok(views.html.profile.updateUserProfile(errors))
        },
        userProfile => {

          // Spara UserProfile
          // var userProfileData = form2Data(userProfile)
          var userProfileData : UserProfileData = new UserProfileData("","")
          userProfileData.id = userProfile.idNo
          userProfileData.userName = userProfile.userName
          userProfileData.emailAddress = userProfile.emailAddress
          userProfileData.firstName = userProfile.firstName
          userProfileData.lastName = userProfile.lastName
          userProfileData.aboutMe = userProfile.aboutMe

          userProfileService.saveUserProfile(userProfileData)

          // Hämta värden
          val savedForm =  userProfileForm.fill(userProfile)
          Ok(views.html.profile.updateUserProfile(savedForm))
        }
      )
  }



  def save = Action {

  val form1   = Form(
    mapping(
      "userName" -> nonEmptyText,
      "emailAddress" -> email,
      "firstName" -> text,
      "lastName" -> text,
      "aboutme" -> text,
      "quality" -> list(boolean),
      "idno" -> longNumber
    )(UserProfile.apply)(UserProfile.unapply)
  )




    System.out.println("SVAR: " +form1)

  /*
    implicit request =>
    userProfileForm.bindFromRequest.fold(
      errors => {
        // Felaktigt ifyllt formulär

           System.out.println("TEST ... TEST... ERROR ")

        val typ = new models.Types
        Ok(views.html.profile.createUserProfile(errors, typ.findAll))
      },
      userProfile => {



        // Spara UserProfile
        /*
        userProfileService.saveUserProfile(
          userProfile.userName,
          userProfile.emailAddress)
        */
        // Hämta värden
        val savedForm  =  userProfileForm.fill(userProfile)

       System.out.println("TEST ... TEST ")



        val typ = new models.Types
        //Ok(views.html.profile.createUserProfile(savedForm, typ.findAll))
        Ok("TEST")
      }
    )
    */
    Ok("TEST")
  }

  def hamtaProfil(userName : String) = Action {
    println("userName : " + userName)
/*
    var up = userProfileService.getUserProfile(userName)
    val enProfileForm
      =  userProfileForm.fill(
        models.formdata.UserProfile(
          up.userName,
          up.emailAddress,
          up.firstName,
          up.lastName,
          up.aboutMe,
          up.id)
    )

)
*/
     val enProfileForm = UserProfile
   //Ok(views.html.profile.updateUserProfile(enProfileForm))
   Ok("testbild")
  }



/******************************************************************************************
 *  Skapa en profil
 *
 */
  def skapaNyProfil = Action {
     var userProfile = models.formdata.UserProfile("","","","","",List(true, true), 0)
     val dufulatValueForm =  userProfileForm.fill(userProfile)
     val typ = new models.Types
    Ok(views.html.profile.createUserProfile(dufulatValueForm, typ.findAll))
  }

}
