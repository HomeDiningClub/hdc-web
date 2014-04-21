package controllers

import org.springframework.beans.factory.annotation.Autowired
import play.api._
import play.api.mvc._
import services.UserProfileService
import play.data.Form
import models.UserProfileData
import org.springframework.stereotype.{Controller => SpringController}
import play.api.data.Form
import play.data.Form

import play.api.data._
import play.api.data.Forms._


import models.formdata.UserProfile

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
      "idno" -> longNumber
    )(UserProfile.apply)(UserProfile.unapply)
  )



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
    implicit request =>
    userProfileForm.bindFromRequest.fold(
      errors => {
        // Felaktigt ifyllt formulär
        Ok(views.html.profile.createUserProfile(errors))
      },
      userProfile => {

        // Spara UserProfile
        userProfileService.saveUserProfile(
          userProfile.userName,
          userProfile.emailAddress)

        // Hämta värden
        val savedForm =  userProfileForm.fill(userProfile)
        Ok(views.html.profile.createUserProfile(savedForm))
      }
    )
  }

  def hamtaProfil(userName : String) = Action {
    println("userName : " + userName)

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

    println("ID  : " + up.id)
    println("firstname  : " + up.firstName)
    println("epostadress  : " + up.emailAddress)

   Ok(views.html.profile.updateUserProfile(enProfileForm))
  }




  def skapaNyProfil = Action {
 /*
      var daniel = userProfileService.getUserProfile("Daniel")
      println("e-post till Daniel: " + daniel.emailAddress)

    var sven = userProfileService.getUserProfile("Sven")
    println("e-post till Sven: " + sven.emailAddress)

    var fredrik = userProfileService.getUserProfile("Fredrik")
    println("e-post till Fredrik: " + fredrik.emailAddress)
*/

     var userProfile = models.formdata.UserProfile("","","","","",0)
     val dufulatValueForm =  userProfileForm.fill(userProfile)
    Ok(views.html.profile.createUserProfile(dufulatValueForm))
  }

}
