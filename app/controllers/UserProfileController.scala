package controllers

// http://www.playframework.com/documentation/2.2.x/ScalaForms

import models.profile.{TagWord, TaggedUserProfile}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.{Controller => SpringController}
import play.api.i18n.Messages
import securesocial.core.{SecuredRequest, SecureSocial}

import services.{RecipeService, CountyService, UserProfileService, TagWordService}
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
import controllers._


@SpringController
class UserProfileController  extends Controller  with SecureSocial {

  // Services
  @Autowired
  var userProfileService: UserProfileService = _

  @Autowired
  var tagWordService : TagWordService = _

  @Autowired
  var countyService : CountyService = _

  @Autowired
  private var recipeService: RecipeService = _


  // Form

  val userProfileForm : play.api.data.Form[models.formdata.UserProfileForm]  = play.api.data.Form(
    mapping(
      "userName" -> text,
      "emailAddress" -> email,
      "firstName" -> text,
      "lastName" -> text,
      "aboutme" -> text,
      "quality" -> list(boolean),
      "county" -> text,
      "streetAddress" -> text,
      "zipCode" -> text,
      "city" -> text,
      "phoneNumber" -> text,
      "idno" -> longNumber
    )(models.formdata.UserProfileForm.apply)(models.formdata.UserProfileForm.unapply)
  )




    val AnvandareForm = Form(
      mapping(
        "name" -> nonEmptyText,
        "quality" -> list(text),
        "aboutmeheadline" -> text,
        "aboutme" -> text,
        "county" -> text,
        "streetAddress" -> text,
        "zipCode" -> text,
        "city" -> text,
        "phoneNumber" -> text
      )
      (EnvData.apply) (EnvData.unapply)
    )

  val tagForm = Form(
    mapping(
      "quality" -> list(text)
    )
      (TagsData.apply) (TagsData.unapply)
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

  private def isThisMyProfile(profile: UserProfile)(implicit request: RequestHeader): Boolean = {
    utils.Helpers.getUserFromRequest match {
      case None =>
        false
      case Some(user) =>
        if(profile.getOwner.objectId == user.objectId)
          true
        else
          false
    }
  }


  def viewProfileByName(profileName: String) = UserAwareAction { implicit request =>

    // Try getting the profile from name, if failure show 404
    userProfileService.findByprofileLinkName(profileName, fetchAll = true) match {
      case Some(profile) =>
        Ok(views.html.profile.index(profile, menuItemsList,FOODANDBEVERAGE,BLOG,REVIEWS,INBOX, recipeBoxes = recipeService.getRecipeBoxes(profile.getOwner), isThisMyProfile = isThisMyProfile(profile)))
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

  var locationId : String = ""


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








  println("county stored : [" +  theUser.county + "]")

  try {
    println("county name: " + theUser.getLocations.iterator().next().county.name)
    locationId = theUser.getLocations.iterator().next().county.objectId.toString
    //locationId = "0ec35cae-495c-43b8-b99c-bc14755288f2"
  } catch
    {
      case e : Exception => println("COUNTY EXCEPTION : " + e.getMessage)
    }

  // File with stored values
  val eData : EnvData = new EnvData(
    theUser.profileLinkName,
  List("", ""),
    theUser.aboutMeHeadline,
    theUser.aboutMe,
    locationId,         // county
    theUser.streetAddress, // street Address,
    theUser.zipCode, // zip code
    theUser.city, // city
    theUser.phoneNumber // phone number
  )

  val nyForm =  AnvandareForm.fill(eData)
  Ok(views.html.profile.skapa(nyForm, retTagList, typ, optionsLocationAreas = getCounties))


}


  /** ******************************************************
    * Show intrests
    * @return
    */

  def showTags = SecuredAction { implicit request =>

    // Fetch UserProfile from UserCredentials that is fetch by SocialSocial
    var theUser = request.user.asInstanceOf[UserCredential].profiles.iterator().next()

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


    // File with stored values
    val tData : TagsData = new TagsData(
      List("adam", "bertil")
    )

    val nyForm =  tagForm.fill(tData)


  //   Ok(views.html.profile.skapa(nyForm, retTagList, typ, optionsLocationAreas = getCounties))
    Ok(views.html.profile.tags(tagForm, retTagList, typ))
  //Ok("test")
  }





  /**
   * Save chaged tages
    * @return
   */
  def saveTags = SecuredAction { implicit request =>

    println("##SAVE TAGS#########################################################################")

    var map: Map[String, String] = Map()


    tagForm.bindFromRequest.fold(
      errors => {
        if (errors.hasErrors) {
          println("1234 Fel lista: " + errors.toString)

          Ok("så fel det kan bli")
        }
      },
      reqUserProfile => {
        // test


        for (d <- reqUserProfile.quality) {
          println("VALD  : " + d)
          map += (d -> d)

        }


      })

    var theUser = request.user.asInstanceOf[UserCredential].profiles.iterator().next()


    theUser.removeAllTags()

    // Fetch all tags available to choose
    var d = tagWordService.listByGroupOption("profile")

    if (d.isDefined) {
      // Loop all available tags
      for (theTag <- d.get) {
        var value = map.getOrElse(theTag.tagName, "empty")

        if (!value.equals("empty")) {

          // If the the user have tagged the particial chooice tag it
          theUser.tag(theTag)
        }

      } // end loop

    }


    println("#########################################################save food interestes ##########################")
    userProfileService.saveUserProfile(theUser)

    Redirect(routes.UserProfileController.showTags())
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
      var aboutMeHeadlineText   : String = ""
      var aboutMeText           : String = ""
      var profileLinkName       : String = ""
      var zipCode               : String = ""
      var streetAddress         : String = ""
      var city                  : String = ""
      var phoneNumber           : String = ""
      var countyId              : String = ""


      AnvandareForm.bindFromRequest.fold(
        errors => {
          if(errors.hasErrors) {
            println("1234 Fel data!")

            println("1234 Fel lista: "  + errors.toString)

            Ok("så fel det kan bli")
          }

          // Felaktigt ifyllt formulär

          Ok("Det blir fel")

        },
        reqUserProfile => {
            // test
            println("Korrekt formulär : " +reqUserProfile.name)

            println("About Me: " + reqUserProfile.aboutme)
          aboutMeHeadlineText   = reqUserProfile.aboutmeheadline
          aboutMeText           = reqUserProfile.aboutme
          profileLinkName       = reqUserProfile.name
          zipCode               = reqUserProfile.zipCode
          streetAddress         = reqUserProfile.streetAddress
          city                  = reqUserProfile.city
          phoneNumber           = reqUserProfile.phoneNumber
          countyId              = reqUserProfile.county
          println("????? county = " + reqUserProfile.county)




        })

        var theUser = request.user.asInstanceOf[UserCredential].profiles.iterator().next()

     theUser.aboutMeHeadline      = aboutMeHeadlineText
     theUser.aboutMe              = aboutMeText
     theUser.profileLinkName      = profileLinkName
     theUser.city                 = city
     theUser.zipCode              = zipCode
     theUser.streetAddress        = streetAddress
     theUser.phoneNumber          = phoneNumber


      theUser.removeAllTags()

      // Fetch all tags available to choose
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

    if(countyId == None || countyId == null || countyId.trim().size < 2) {
      theUser.removeLocation()
    } else {
      countyService.findById(UUID.fromString(countyId)) match {
        case None => // Do something when nothing found
          println("county name: NONE value ")
          theUser.removeLocation()
        case Some(item) =>
          theUser.removeLocation()
          theUser.locate(item)
          println("county name: " + item.name)
          println("OBJECTID: " + item.objectId + " def :  " + item.toString)

      }
    }




  // save the new UserProfiel

      println("#########################################################save profile ##########################")
      userProfileService.saveUserProfile(theUser)

     Redirect(routes.UserProfileController.edit())
}



  def saveUserProfile = Action { implicit request =>
      userProfileForm.bindFromRequest.fold(
        errors => {

          if(errors.hasErrors) {
            println("Error in Form : "  + errors.toString)
          }
          println("#error#")
          // Felaktigt ifyllt formulär
          // Ok(views.html.profile.updateUserProfile(errors))
          Ok("eror")
        },
        userProfileForm => {

          // Spara UserProfile
          // var userProfileData = form2Data(userProfile)
          var userProfile : models.UserProfile = new models.UserProfile

          //userProfileService.saveUserProfile(userProfile)

          // Hämta värden
          //val savedForm =  userProfileForm.fill()
          // Ok(views.html.profile.updateUserProfile())
          Ok(".............................................")
        }
      )
  }

}
