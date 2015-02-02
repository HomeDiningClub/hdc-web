package controllers

import models.content.ContentPage
import play.api.mvc._
import org.springframework.stereotype.{Controller => SpringController}
import play.api.mvc.Controller
import play.api.data._
import play.api.data.Forms._
import models.viewmodels.{ReviewBox, StartPageBox, SearchStartPageForm}
import securesocial.core.SecureSocial
import org.springframework.beans.factory.annotation.Autowired
import services._
import models.{UserCredential, UserProfile}
import models.rating.RatesUserCredential
import views.html.helper.{select, options}
import models.profile.TagWord
import play.api.i18n.Messages
import scala.collection.mutable
import models.location.County
import java.util.UUID
import scala.collection.JavaConverters._


@SpringController
class StartPageController extends Controller with SecureSocial {

  @Autowired
  var userProfileService: UserProfileService = _

  @Autowired
  var tagWordService : TagWordService = _

  @Autowired
  var countyService : CountyService = _

  @Autowired
  var contentService : ContentService = _

  @Autowired
  var ratingService: RatingService = _

  // Search form
  val searchForm = Form(
    mapping(
      //"freeText" -> optional(text),
      "fCounty" -> optional(text),
      "fTag" -> optional(text),
      "fHost" -> optional(boolean)
    )(SearchStartPageForm.apply)(SearchStartPageForm.unapply)
  )

  def index(fTag: String, fCounty: String, fHost: Boolean) = UserAwareAction { implicit request =>

    val startPageBoxes = getStartPageBoxes(fTag, fCounty, fHost, 8)
    val form = SearchStartPageForm.apply(
      fCounty match { case null | "" => None case item => Some(item)},
      fTag match { case null | "" => None case item => Some(item)},
      fHost match { case false => None case item => Some(item)}
    )

    Ok(views.html.startpage.index(
      searchForm = searchForm.fill(form),
      optionsFoodAreas = tagWordService.getFoodAreas,
      optionsLocationAreas = countyService.getCounties,
      optionsIsHost = if(fHost) Some(true) else Some(false),
      startPageBoxes = startPageBoxes,
      reviewBoxes = ratingService.getUserReviewBoxesStartPage(4),
      asideNews = contentService.getAsideNewsItems
    ))
  }

  private def getStartPageBoxes(boxFilterTag: String, boxFilterCounty: String, boxFilterIsHost: Boolean, maxNr: Int = 8): Option[List[StartPageBox]] = {

    val fetchedTag: Option[TagWord] = boxFilterTag match {
      case "" | null => None
      case tagWord =>
        tagWordService.findById(UUID.fromString(tagWord))

    }

    val fetchedCounty: Option[County] = boxFilterCounty match {
      case "" | null => None
      case county =>
        countyService.findById(UUID.fromString(county))
    }

    val startPageBoxes: Option[List[StartPageBox]] = userProfileService.getUserProfilesFiltered(filterTag = fetchedTag, filterCounty = fetchedCounty, filterIsHost = boxFilterIsHost).asInstanceOf[Option[List[UserProfile]]] match {
      case None => None
      case Some(profile) => Some(profile.filter(prof => prof.getMainImage != null).take(maxNr).map {
        userProfile: UserProfile =>
          StartPageBox(
            objectId = Some(userProfile.objectId),
            linkToProfile = userProfile.profileLinkName match {
              case null => ""
              case pfName => routes.UserProfileController.viewProfileByName(pfName).url
            },
            fullName = userProfile.getOwner.firstName,
            location = userProfile.getLocations.asScala.headOption match {
              case None => None
              case Some(countyTag) => Some(countyTag.county.name)
            },
            mainBody = None,
            mainImage = userProfile.getMainImage match {
              case null => None
              case image => Some(routes.ImageController.profileBox(image.getStoreId).url)
            },
            userImage = userProfile.getAvatarImage match {
              case null => None
              case image => Some(routes.ImageController.userThumb(image.getStoreId).url)
            },
            userRating = userProfile.getOwner.getAverageRating,
            isHost = userProfile.isUserHost
          )
      })
    }
    startPageBoxes
  }



}


