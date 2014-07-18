package controllers

import play.api.mvc._
import org.springframework.stereotype.{Controller => SpringController}
import play.api.mvc.Controller
import play.api.data._
import play.api.data.Forms._
import models.viewmodels.{StartPageBox, SearchStartPageForm}
import securesocial.core.SecureSocial
import org.springframework.beans.factory.annotation.Autowired
import services.{TagWordService, UserProfileService}
import models.UserProfile
import models.rating.RatingUserCredential
import views.html.helper.{select, options}
import models.profile.TagWord
import play.api.i18n.Messages
import scala.collection.mutable


@SpringController
class StartPageController extends Controller with SecureSocial {

  @Autowired
  var userProfileService: UserProfileService = _

  @Autowired
  var tagWordService : TagWordService = _

  // Search startpage form
  val searchStartPageForm = Form(
    mapping(
      //"freeText" -> optional(text),
      "area" -> optional(text),
      "foodArea" -> optional(text)
    )(SearchStartPageForm.apply _)(SearchStartPageForm.unapply _)
  )

  def filterProfiles = Action { implicit request =>
    Ok(views.html.startpage.index(
      searchForm = searchStartPageForm,
      optionsFoodAreas = getFoodAreas,
      optionsLocationAreas = None,
      startPageBoxes = getStartPageBoxes))
  }

  def index = UserAwareAction { implicit request =>
    Ok(views.html.startpage.index(
      searchForm = searchStartPageForm,
      optionsFoodAreas = getFoodAreas,
      optionsLocationAreas = None,
      startPageBoxes = getStartPageBoxes))
  }


  private def getFoodAreas: Option[Seq[(String,String)]] = {
    val foodTags: Option[Seq[(String,String)]] = tagWordService.listByGroupOption("profile") match {
      case Some(listOfTags) =>
        var bufferList : mutable.Buffer[(String,String)] = mutable.Buffer[(String,String)]()

        // Prepend the fist selection
        bufferList += (("", Messages("startpage.filterform.foodarea")))

        // Map and add the rest
        listOfTags.sortBy(tw => tw.tagName).toBuffer.map {
          tag: TagWord =>
            bufferList += ((tag.objectId.toString, tag.tagName))
        }

        Some(bufferList.toSeq)
      case None =>
        None
    }

    foodTags
  }

  private def getStartPageBoxes: Option[List[StartPageBox]] = {
    val startPageBoxes: List[StartPageBox] = userProfileService.getAllUserProfile.map {
      userProfile: UserProfile =>
        StartPageBox(
          objectId = Some(userProfile.objectId),
          linkToProfile = routes.HostController.indexHost(userProfile.objectId).url,
          fullName = "Full name", // userProfile.userCredential.fullName
          location = "User location",
          mainBody = None,
          mainImage = routes.Assets.at("images/startpage/Box2.png").url, //mainImage = userProfile.mainImage.getTransformByName("box").url,
          userImage = routes.Assets.at("images/host/host-head-example-100x100.jpg").url, //userImage = //mainImage = userProfile.userImage.getTransformByName("thumbnail").url,
          userRating = 5)
    }

    if(startPageBoxes.isEmpty)
      None
    else
      Some(startPageBoxes)
  }
}