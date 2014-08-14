package controllers

import play.api.mvc._
import org.springframework.stereotype.{Controller => SpringController}
import play.api.mvc.Controller
import play.api.data._
import play.api.data.Forms._
import models.viewmodels.{ReviewBox, StartPageBox, SearchStartPageForm}
import securesocial.core.SecureSocial
import org.springframework.beans.factory.annotation.Autowired
import services.{RatingService, CountyService, TagWordService, UserProfileService}
import models.{UserCredential, UserProfile}
import models.rating.RatingUserCredential
import views.html.helper.{select, options}
import models.profile.TagWord
import play.api.i18n.Messages
import scala.collection.mutable
import models.location.County


@SpringController
class StartPageController extends Controller with SecureSocial {

  @Autowired
  var userProfileService: UserProfileService = _

  @Autowired
  var tagWordService : TagWordService = _

  @Autowired
  var countyService : CountyService = _

  @Autowired
  var ratingService: RatingService = _

  // Search startpage form
  val searchStartPageForm = Form(
    mapping(
      //"freeText" -> optional(text),
      "area" -> optional(text),
      "foodArea" -> optional(text)
    )(SearchStartPageForm.apply)(SearchStartPageForm.unapply)
  )

  def filterProfiles = Action { implicit request =>
    Ok(views.html.startpage.index(
      searchForm = searchStartPageForm,
      optionsFoodAreas = getFoodAreas,
      optionsLocationAreas = getCounties,
      startPageBoxes = getStartPageBoxes,
      reviewBoxes = getReviewBoxes
    ))
  }

  def index = UserAwareAction { implicit request =>
    Ok(views.html.startpage.index(
      searchForm = searchStartPageForm,
      optionsFoodAreas = getFoodAreas,
      optionsLocationAreas = getCounties,
      startPageBoxes = getStartPageBoxes,
      reviewBoxes = getReviewBoxes
    ))
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



  private def getStartPageBoxes: Option[List[StartPageBox]] = {
    val startPageBoxes: List[StartPageBox] = userProfileService.getAllUserProfile.map {
      userProfile: UserProfile =>
        StartPageBox(
          objectId = Some(userProfile.objectId),
          linkToProfile = userProfile.profileLinkName match {
            case null => ""
            case pfName => routes.UserProfileController.viewProfileByName(pfName).url
          },
          fullName = userProfile.getOwner.fullName,
          location = userProfile.county,
          mainBody = None,
          mainImage = routes.Assets.at("images/startpage/Box2.png").url, //mainImage = userProfile.mainImage.getTransformByName("box").url,
          userImage = routes.Assets.at("images/host/host-head-example-100x100.jpg").url, //userImage = //mainImage = userProfile.userImage.getTransformByName("thumbnail").url,
          userRating = userProfile.getOwner.getAverageRating)
    }

    if(startPageBoxes.isEmpty)
      None
    else
      Some(startPageBoxes)
  }

  private def getReviewBoxes: Option[List[ReviewBox]] = {
    ratingService.findRatingByRatingValue(4) match {
      case None => None
      case Some(items) =>
        Some{ items.take(4).map { ratingItem: RatingUserCredential =>
          ReviewBox(
            objectId = Some(ratingItem.objectId),
            linkToProfile = ratingItem.userWhoIsRating.profiles.iterator().next().profileLinkName match { // TODO: Review-boxes links quite ugly
              case null => ""
              case pfName => routes.UserProfileController.viewProfileByName(pfName).url
            },
            fullName = ratingItem.userWhoIsRating.fullName,
            reviewText = Some(ratingItem.ratingComment),
            userImage = routes.Assets.at("images/host/host-head-example-100x100.jpg").url, //ratingItem.userWhoIsRating.userImage.getTransformByName("thumbnail").url
            rating = ratingItem.ratingValue)}
        }
    }
  }


}


