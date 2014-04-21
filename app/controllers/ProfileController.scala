package controllers

import org.springframework.beans.factory.annotation.Autowired
import play.api._
import play.api.mvc._
import services.UserProfileService
import models.UserProfileData
import org.springframework.stereotype.{Controller => SpringController}



object ProfileController extends Controller{

  @Autowired
  var userProfileService : UserProfileService = _

  def index = Action {
    val userProfilesList = userProfileService.getAllUserProfiles()
    Ok(views.html.profile.listofProfiles(userProfilesList))
  }



  def listUserProfiles = Action {

    val userProfilesList = userProfileService.getAllUserProfiles()

    for(userProfile <- userProfilesList ) {
      println(userProfile.emailAddress)
    }

   Ok(views.html.profile.listofProfiles(userProfilesList))
  }

}
