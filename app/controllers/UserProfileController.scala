package controllers

// http://www.playframework.com/documentation/2.2.x/ScalaForms

import models.profile.{TagWord, TaggedUserProfile}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.{Controller => SpringController}
import play.api.i18n.Messages
import securesocial.core.{SecuredRequest, SecureSocial}

import services.{CountyService, UserProfileService, TagWordService}
import models.{UserProfile, UserCredential}
import models.formdata.UserProfileForm

import play.api.data._
import play.api.data.Forms._

import play.api._
import play.api.mvc._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import models.location.County
import java.util.UUID
import enums.RoleEnums
import utils.authorization.WithRole
import constants.FlashMsgConstants


@SpringController
class UserProfileController  extends Controller  with SecureSocial {

  // Services

  @Autowired
  var userProfileService: UserProfileService = _

  @Autowired
  var tagWordService : TagWordService = _


  @Autowired
  var countyService : CountyService = _


  // Form

  val userProfileForm : play.api.data.Form[models.formdata.UserProfileForm]  = play.api.data.Form(
    mapping(
      "userName" -> nonEmptyText,
      "emailAddress" -> email,
      "firstName" -> text,
      "lastName" -> text,
      "aboutme" -> text,
      "quality" -> list(boolean),
      "county" -> text,
      "streetAddress" -> nonEmptyText,
      "zipCode" -> nonEmptyText,
      "city" -> nonEmptyText,
      "phoneNumber" -> text,
      "idno" -> longNumber
    )(models.formdata.UserProfileForm.apply)(models.formdata.UserProfileForm.unapply)
  )




    val AnvandareForm = Form(
      mapping(
        "name" -> text,
        "quality" -> list(text),
        "aboutmeheadline" -> text,
        "aboutme" -> text,
        "county" -> text,
        "streetAddress" -> nonEmptyText,
        "zipCode" -> nonEmptyText,
        "city" -> nonEmptyText,
        "phoneNumber" -> text
      )
      (EnvData.apply) (EnvData.unapply)
    )



  // Constants
  val FOODANDBEVERAGE = "foodandbeverage-tab"
  val BLOG = "blog-tab"
  val REVIEWS = "reviews-tab"
  val INBOX = "inbox-tab"

  // Link-name, title, link-href, class-name, active
  val menuItemsList = Seq[(String,String,String,String)](
    ("Mat & Dryck", "Mat & Dryck", FOODANDBEVERAGE, "active"),
    ("Blogg", "Blogg", BLOG, ""),
    ("Omdömen", "Omdömen", REVIEWS, ""),
    ("Inbox", "Inbox", INBOX, "")
  )

//  def index = Action { implicit request =>
//    Ok(views.html.host.index(menuItemsList,FOODANDBEVERAGE,BLOG,REVIEWS,INBOX))
//  }
//
//  def indexWithID(objectId: UUID) = Action { implicit request =>
//    Ok(views.html.host.index(menuItemsList,FOODANDBEVERAGE,BLOG,REVIEWS,INBOX))
//  }

//  def viewProfile(profile: UserProfile) = Results { implicit request =>
//
//    // Try getting the profile from name, if failure show 404
//    profile match {
//      case Some(profile) =>
//        Ok(views.html.profile.index(profile, menuItemsList,FOODANDBEVERAGE,BLOG,REVIEWS,INBOX))
//      case None =>
//        NotFound("Cannot find user profile")
//    }
//  }

  def viewProfileByName(profileName: String) = UserAwareAction { implicit request =>

    // Try getting the profile from name, if failure show 404
    userProfileService.findByprofileLinkName(profileName) match {
      case Some(profile) =>
        Ok(views.html.profile.index(profile, menuItemsList,FOODANDBEVERAGE,BLOG,REVIEWS,INBOX))
      case None =>
        val errMess = "Cannot find user profile using name:" + profileName
        Logger.debug(errMess)
        NotFound(errMess)
    }
  }


  def viewProfileByLoggedInUser = SecuredAction(authorize = WithRole(RoleEnums.USER)) { implicit request: RequestHeader =>

    SecureSocial.currentUser match {
      case Some(reqUser) =>
        userProfileService.findByowner(reqUser.asInstanceOf[UserCredential]) match {
          case Some(profile) =>
            if(profile.profileLinkName.isEmpty){
              Logger.debug("Profilelinkname is empty!")
              Redirect(routes.UserProfileController.edit()).flashing(FlashMsgConstants.Error -> Messages("profile.profilelinkname.isempty"))
            }else{
              Redirect(routes.UserProfileController.viewProfileByName(profile.profileLinkName)) // TODO: This causes double lookup, improve later
            }
          case None =>
            val errMess = "Cannot find user profile using current user:" + reqUser.asInstanceOf[UserCredential].objectId
            Logger.debug(errMess)
            NotFound(errMess)
        }
      case None =>
        val errMess = "Cannot find any user to fetch profile for"
        Logger.debug(errMess)
        NotFound(errMess)
    }
  }


def createTags = Action { implicit request =>

  // MATCH (b:TagWord) RETURN b
  // MATCH (b:TagWord) delete b


  // create userPrifile
  var u: models.UserProfile = new models.UserProfile
  u.aboutMe = "test"

  println("start ....")
  //  userProfileService.saveUserProfile(u)


  tagWordService.createTag("Amerikanskt", "Amerikanskt", "quality[0]", "test")
  tagWordService.createTag("Amerikanskt", "Amerikanskt", "quality[0]", "profile")
  tagWordService.createTag("Italienskt", "Italienskt", "quality[1]", "profile")
  tagWordService.createTag("Franskt", "Franskt", "quality[2]", "profile")
  tagWordService.createTag("Asiatiskt", "Asiatiskt", "quality[3]", "profile")
  tagWordService.createTag("Svensk husman", "Svensk husman", "quality[4]", "profile")
  tagWordService.createTag("Mellanöstern", "Mellanöstern", "quality[5]", "profile")
  tagWordService.createTag("Vegetarisk", "Vegetarisk", "quality[6]", "profile")
  tagWordService.createTag("RAW-food", "RAW-food", "quality[7]", "profile")
  tagWordService.createTag("LCHF", "LCHF", "quality[8]", "profile")
  tagWordService.createTag("Koscher", "Koscher", "quality[9]", "profile")
  tagWordService.createTag("Vilt", "Vilt", "quality[10]", "profile")
  tagWordService.createTag("Kött", "Kött", "quality[11]", "profile")
  tagWordService.createTag("Fisk och skaldjur", "Fisk och skaldjur", "quality[12]", "profile")
  tagWordService.createTag("Lyx", "Lyx", "quality[13]", "profile")
  tagWordService.createTag("Budget", "Budget", "quality[14]", "profile")
  tagWordService.createTag("Barnvänligt", "Barnvänligt", "quality[15]", "profile")
  tagWordService.createTag("Friluftsmat", "Friluftsmat", "quality[16]", "profile")
  tagWordService.createTag("Drycker", "Drycker", "quality[17]", "profile")
  tagWordService.createTag("Efterrätter", "Efterrätter", "quality[18]", "profile")
  tagWordService.createTag("Bakverk", "Bakverk", "quality[19]", "profile")


  val lista = tagWordService.listByGroupOption("profile")
  var v: StringBuilder = new StringBuilder


  if(lista.isDefined){
    for (a <- lista.get) {
      v.append("\n")
      v.append(a.tagName)
      v.append(", ")
      v.append(a.tagId)
      v.append(", ")
      v.append(a.orderId)
    }
  }


 Ok(v.toString())
}


  private def getCounties: Option[Seq[(String,String)]] = {
    val counties: Option[Seq[(String,String)]] = countyService.getListOfAll match {
      case Some(counties) =>
        var bufferList : mutable.Buffer[(String,String)] = mutable.Buffer[(String,String)]()

        // Prepend the first selection
        bufferList += (("", Messages("startpage.filterform.counties")))

        // Map and add the rest
        counties.sortBy(tw => tw.name).toBuffer.map {
          item: County =>
            bufferList += ((item.objectId.toString, item.name))
        }

        Some(bufferList.toSeq)
      case None =>
        None
    }

    counties
  }






// Edit Profile
def edit = SecuredAction { implicit request =>


  // Fetch UserProfile from UserCredentials
  var theUser = request.user.asInstanceOf[UserCredential].profiles.iterator().next()

  var countiesList = getCounties

  var countyItter = countiesList.iterator
  while(countyItter.hasNext) {
    var nextCo = countyItter.next()
  }


  val provider = request.user.identityId.providerId
  val user = request.user.identityId.userId


  // show profile

  // 1. Get the correct profile

  // 2. Load the tags


  // Pre selected
  val typ = new models.Types

var userTags = theUser.getTags

if(userTags != null) {
  var itterTags = userTags.iterator()

  while (itterTags.hasNext) {
    typ.addVald(itterTags.next().tagWord.tagName)
  }
}

  // Fetch all tags
  var d = tagWordService.listByGroupOption("profile")


  var l : Long = 0
  var tagList : mutable.HashSet[models.Type] = new mutable.HashSet[models.Type]()



  if(d.isDefined){
    for(theTag <- d.get) {
      var newType : models.Type = new models.Type(l, theTag.tagName, theTag.tagName, "quality[" + l+"]" )
      l = l + 1
      println(theTag.tagName)
      tagList.add(newType)
    }
  }

  // Sort & List
  val retTagList = tagList.toList.sortBy(tw => tw.name)

  // member

  /*
  var memberList = tagWordService.listByGroupOption("member")

  if(memberList.isDefined) {
    for(theMemberStatus <- memberList.get) {
      println("Member status" + theMemberStatus.orderId + ", " + theMemberStatus.tagName + ", " + theMemberStatus.tagId)
    }
  }

*/

  println("DEBUG::::: 1111 ")

  // File with stored values
  val eData : EnvData = new EnvData(
    theUser.profileLinkName,
    List("adam", "bertil"),
    theUser.aboutMeHeadline,
    theUser.aboutMe,
    theUser.county,
    "", //theUser.streetAddress,
    "theUser.zipCode",
    "theUser.city",
    ""
  )

  println("DEBUG::::: 222 ")
  val nyForm =  AnvandareForm.fill(eData)
  Ok(views.html.profile.skapa(nyForm, retTagList, typ, optionsLocationAreas = getCounties))


}




// Save profile
def editSubmit = SecuredAction { implicit request =>

  println("************************ save profile *********************************************")


  //var service = new services.UserProfileService()

      // Fetch user

      // Fetch usersprofile

      // remove all tags

      // add all new tags

      var map:Map[String,String] = Map()
      var aboutMeHeadlineText : String = ""
      var aboutMeText : String = ""
      var profileLinkName : String = ""

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

            println("About Me: " + anvadare.aboutme)
          aboutMeHeadlineText = anvadare.aboutmeheadline
          aboutMeText = anvadare.aboutme
          profileLinkName= anvadare.name


          println("????? county = " + anvadare.county)
          var c   = countyService.getListOfAll.get

          for(d <- c) {
            println("NAME: " + d.name)
            println("OBJECTID: " + d.objectId + " - " + anvadare.county + " - " + d.objectId.equals(anvadare.county))
          }

          println("Size :::: " + c.size)


         // var linkedUser = service.findByProfileLinkName(profileLinkName)



            for(d <- anvadare.quality) {
                println("VALD  : " + d)
                map += (d->d)

            }


        })

        var theUser = request.user.asInstanceOf[UserCredential].profiles.iterator().next()

     theUser.aboutMeHeadline = aboutMeHeadlineText
     theUser.aboutMe = aboutMeText
     theUser.profileLinkName = profileLinkName
     theUser.city  = ""
     theUser.streetAddress = ""
     theUser.zipCode = ""

      theUser.removeAllTags()

      // Fetch all tags
      var d = tagWordService.listByGroupOption("profile")

      if(d.isDefined){

        // Loop all available tags
        for(theTag <- d.get) {
          var value = map.getOrElse(theTag.tagName, "empty")

          if(!value.equals("empty")) {

            // If the the user have tagged the particial chooice tag it
            theUser.tag(theTag)
          }

        } // end loop

      }

      userProfileService.saveUserProfile(theUser)

     Redirect(routes.UserProfileController.edit())
}



  def saveUserProfile = Action { implicit request =>
      userProfileForm.bindFromRequest.fold(
        errors => {

          if(errors.hasErrors) {
            println("Fel data!")

            println("Fel lista: "  + errors.toString)


          }

          // Felaktigt ifyllt formulär
          Ok(views.html.profile.updateUserProfile(errors))
        },
        userProfileForm => {

          // Spara UserProfile
          // var userProfileData = form2Data(userProfile)
          var userProfile : models.UserProfile = new models.UserProfile

          //userProfileService.saveUserProfile(userProfile)

          // Hämta värden
          //val savedForm =  userProfileForm.fill()
          // Ok(views.html.profile.updateUserProfile())
          Ok("TEST")
        }
      )
  }

}
