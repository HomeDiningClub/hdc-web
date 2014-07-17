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


@SpringController
class StartPageController extends Controller with SecureSocial {

  @Autowired
  var userProfileService: UserProfileService = _

  @Autowired
  var tagWordService : TagWordService = _

  // Search startpage form
//  val searchStartPageForm = Form(
//    mapping(
//      "freeText" -> optional(text),
//      "area" -> Seq[(String, String)],
//      "foodArea" -> Seq[(String, String)]
//    )(SearchStartPageForm.apply _)(SearchStartPageForm.unapply _)
//  )

  def filterProfiles = Action { implicit request =>
    Ok(views.html.startpage.index())
    //Ok(views.html.startpage.index(searchStartPageForm))
  }

  def index = UserAwareAction { implicit request =>




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

    if(!startPageBoxes.isEmpty)
      Ok(views.html.startpage.index(startPageBoxes))
    else
      Ok(views.html.startpage.index())
  }

}