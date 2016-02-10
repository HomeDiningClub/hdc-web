package controllers

import javax.inject.{Named, Inject}

import org.springframework.stereotype.{Controller => SpringController}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Controller
import play.api.data._
import play.api.data.Forms._
import models.viewmodels.StartPageBox
import org.springframework.beans.factory.annotation.Autowired
import securesocial.core.SecureSocial
import services._
import models.{UserCredential, UserProfile}

import views.html.helper.{select, options}
import models.profile.TagWord
import models.location.County
import java.util.UUID
import scala.collection.JavaConverters._
import customUtils.security.SecureSocialRuntimeEnvironment
import models.formdata.SearchStartPageForm

class StartPageController @Inject() (override implicit val env: SecureSocialRuntimeEnvironment,
                                     val userProfileService: UserProfileService,
                                     val tagWordService: TagWordService,
                                     val countyService: CountyService,
                                     val ratingService: RatingService,
                                     val contentService: ContentService,
                                     val messagesApi: MessagesApi) extends Controller with SecureSocial with I18nSupport {

  /*
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
*/

  // Search form
  val searchForm = Form(
    mapping(
      //"freeText" -> optional(text),
      "fCounty" -> optional(text),
      "fTag" -> optional(text),
      "fHost" -> optional(boolean)
    )(SearchStartPageForm.apply)(SearchStartPageForm.unapply)
  )

  def index(fTag: String, fCounty: String, fHost: Int) = UserAwareAction() { implicit request =>

    val isHost = if(fHost == 1) true else false

    val startPageBoxes = getStartPageBoxes(fTag, fCounty, isHost, 8)
    val form = SearchStartPageForm.apply(
      fCounty match { case null | "" => None case item => Some(item)},
      fTag match { case null | "" => None case item => Some(item)},
      isHost match { case false => None case item => Some(item)}
    )

    Ok(views.html.startpage.index(
      searchForm = searchForm.fill(form),
      optionsFoodAreas = tagWordService.getFoodAreas,
      optionsLocationAreas = countyService.getCounties,
      optionsIsHost = if(isHost) Some(true) else Some(false),
      startPageBoxes = startPageBoxes,
      reviewBoxes = None, //ratingService.getUserReviewBoxesStartPage(4), //TODO: Fix reviewBoxes speed
      asideNews = contentService.getAsideNewsItems,
      currentUser = request.user
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
            fullName = userProfile.profileLinkName,
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


