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
      "emailAddress" -> email
    )(UserProfile.apply)(UserProfile.unapply)
  )



  def index = Action {
    val userProfilesList = userProfileService.getAllUserProfiles()
    Ok(views.html.profile.listofProfiles(userProfilesList))
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
    val enProfileForm =  userProfileForm.fill(models.formdata.UserProfile(up.userName, up.emailAddress))
    Ok(views.html.profile.createUserProfile(enProfileForm))
  }




  def skapaNyProfil = Action {

      var daniel = userProfileService.getUserProfile("Daniel")
      println("e-post till Daniel: " + daniel.emailAddress)

    var sven = userProfileService.getUserProfile("Sven")
    println("e-post till Sven: " + sven.emailAddress)

    var fredrik = userProfileService.getUserProfile("Fredrik")
    println("e-post till Fredrik: " + fredrik.emailAddress)



    //    val userProfilesList = userProfileService.getAllUserProfile()
//    var antal = userProfilesList.size
//    println("Antal: " + antal)

     val dufulatValueForm =  userProfileForm.fill(models.formdata.UserProfile("", ""))
    Ok(views.html.profile.createUserProfile(dufulatValueForm))
  }

}
