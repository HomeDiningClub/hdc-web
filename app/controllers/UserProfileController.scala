package controllers

// http://www.playframework.com/documentation/2.2.x/ScalaForms

import models.profile.TaggedUserProfile
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.{Controller => SpringController}

import services.UserProfileService
import models.UserProfile
import models.formdata.UserProfile

import services.TagWordService

import play.api.data._
import play.api.data.Forms._

import play.api._
import play.api.mvc._

import scala.collection.mutable.ListBuffer


@SpringController
class UserProfileController  extends Controller{

  @Autowired
  var userProfileService: UserProfileService = _

  @Autowired
  var tagWordService : TagWordService = _





  val userProfileForm : play.api.data.Form[models.formdata.UserProfile]  = play.api.data.Form(
    mapping(
      "userName" -> nonEmptyText,
      "emailAddress" -> email,
      "firstName" -> text,
      "lastName" -> text,
      "aboutme" -> text,
      "quality" -> list(boolean),
      "idno" -> longNumber
    )(models.formdata.UserProfile.apply)(models.formdata.UserProfile.unapply)
  )




    val AnvandareForm = Form(
      mapping(
        "name" -> text,
        "emails" -> list(text),
        "quality" -> list(text)
      )
      (controllers.EnvData.apply) (controllers.EnvData.unapply)
    )




def createTags = Action {

  // MATCH (b:TagWord) RETURN b
  // MATCH (b:TagWord) delete b

/*
  tagWordService.createTag("Amerikanskt", "Amerikanskt", "quality[0]", "profile")
  tagWordService.createTag("Italienskt", "Italienskt", "quality[1]", "profile")
  tagWordService.createTag("Franskt", "Franskt", "quality[2]", "profile")
  tagWordService.createTag("Asiatiskt", "Asiatiskt", "quality[3]", "profile")
  tagWordService.createTag("Svensk husman", "Svensk husman", "quality[4]", "profile")
  tagWordService.createTag("Mellanöstern", "Mellanöstern", "quality[5]", "profile")
  tagWordService.createTag( "Vegetarisk", "Vegetarisk", "quality[6]", "profile")
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
*/



var lista = tagWordService.listAll()
//var lista = tagWordService.listByGroup("profile")
  var v : StringBuilder = new StringBuilder


  for( a <- lista) {
    v.append("\n")

    v.append(a.tagName)
    v.append(", ")
    v.append(a.tagId)
    v.append(", ")
    v.append(a.orderId)


  }


 Ok(v.toString())
}



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

            println("Do, antal : " + anvadare.quality.size)
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
    //Ok(views.html.profile.listofProfiles(userProfilesList))
    Ok("OK")
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
    )(models.formdata.UserProfile.apply)(models.formdata.UserProfile.unapply)
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
     val enProfileForm = models.formdata.UserProfile
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


  def test = Action {
    var user: models.UserProfile = new models.UserProfile()
   // user.userId = "test"
   //user.providerId = "test"

    var userList = userProfileService.getAllUserProfiles()
    var tagList = tagWordService.listAll()

    if (userList.size > 0) {

      println("Users : " +  userList.size)


      var up = userList.tail.tail.head

      var antal: Int = 0
      /*
      var tagItter = up.getUserProfileTags.iterator()


      var list: ListBuffer[TaggedUserProfile] = new ListBuffer[TaggedUserProfile]()

      while (tagItter.hasNext) {
        var d = tagItter.next()
        println("TAG NAME: " + d.tagWord.tagName + ", " + d.tagWord.tagId + ", " + d.tagWord.orderId)
        list += d
      }
*/
      // remove all tagwords to profile
      /*
      for (vx <- list) {
        println("\ntest :" + vx.tagWord.orderId)
        up.remove(vx)
      }
      userProfileService.saveUserProfile(up)
      */


    // add tagwords to profile
    /*
    up.aboutMe = "vinter"
    up.isHost = true
      println("Key: " + up.key)

    for( tag <- tagList) {
      up.memberOf(tag)
    }
    userProfileService.saveUserProfile(up)
    */

      // userProfileService.saveUserProfile("","")


    }

    Ok("OK")
  }

}
