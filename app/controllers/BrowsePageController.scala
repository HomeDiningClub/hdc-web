package controllers

import java.util.UUID

import models.UserProfile
import models.location.County
import models.profile.TagWord
import models.viewmodels.{BrowseBox, SearchFilterForm}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.stereotype.{Controller => SpringController}
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.mvc.Controller
import securesocial.core.SecureSocial
import services._
import scala.collection.JavaConverters._


@SpringController
class BrowsePageController extends Controller with SecureSocial {

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
    )(SearchFilterForm.apply)(SearchFilterForm.unapply)
  )

  def index(fTag: String, fCounty: String, fHost: Boolean) = UserAwareAction { implicit request =>

    //val browseBoxes = getBrowseBoxes(fTag, fCounty, fHost)
    val form = SearchFilterForm.apply(
      fCounty match { case null | "" => None case item => Some(item)},
      fTag match { case null | "" => None case item => Some(item)},
      fHost match { case false => None case item => Some(item)}
    )

    Ok(views.html.browse.index(
      searchForm = searchForm.fill(form),
      optionsFoodAreas = tagWordService.getFoodAreas,
      optionsLocationAreas = countyService.getCounties,
      optionsIsHost = if(fHost) Some(true) else Some(false)
      //browseBoxes = browseBoxes
    ))
  }

  def getBrowseBoxesPagedJSON(boxFilterTag: String, boxFilterCounty: String, boxFilterIsHost: Boolean, page: Int = 0) = UserAwareAction { implicit request =>

    // Get items
    val listOfBoxes: Option[Page[UserProfile]] = getBrowseBoxesPaged(boxFilterTag, boxFilterCounty, boxFilterIsHost, page)

    listOfBoxes match {
      case Some(list) =>
        if (list.hasContent) {
          // First fetch the list of items
          var returnString = views.html.browse.browseBox.render(buildBoxes(list.asScala.toList))
          // Attach pagination if more then one page
          if (list.getTotalPages > 1) {
            returnString += views.html.shared.pagination.render(jsMethodName = "getBoxesAsJSON", hasNext = list.hasNext, hasPrev = list.hasPrevious, currentPage = page, totalCount = list.getTotalElements, totalPages = list.getTotalPages)
          }
          Ok(returnString)
        }else{
          Ok(views.html.browse.browseNoHit.render(Messages("browse.boxes.not-found", routes.BrowsePageController.index().url)))
        }
      case None =>
        Ok(views.html.browse.browseNoHit.render(Messages("browse.boxes.not-found", routes.BrowsePageController.index().url)))
    }
  }

  private def getBrowseBoxesPaged(boxFilterTag: String, boxFilterCounty: String, boxFilterIsHost: Boolean, pageNo: Int): Option[Page[UserProfile]] = {
    userProfileService.getUserProfilesFiltered(filterTag = fetchTag(boxFilterTag), filterCounty = fetchCounty(boxFilterCounty), filterIsHost = boxFilterIsHost, Some(pageNo), 12).asInstanceOf[Option[Page[UserProfile]]]
  }

  private def getBrowseBoxes(boxFilterTag: String, boxFilterCounty: String, boxFilterIsHost: Boolean): Option[List[UserProfile]] = {
    userProfileService.getUserProfilesFiltered(filterTag = fetchTag(boxFilterTag), filterCounty = fetchCounty(boxFilterCounty), filterIsHost = boxFilterIsHost).asInstanceOf[Option[List[UserProfile]]]
  }

  // Helper methods
  private def fetchTag(boxFilterTag: String): Option[TagWord] ={
    boxFilterTag match {
      case "" | null => None
      case tagWord =>
        tagWordService.findById(UUID.fromString(tagWord))
    }
  }

  private def fetchCounty(boxFilterCounty: String): Option[County] ={
    boxFilterCounty match {
      case "" | null => None
      case county =>
        countyService.findById(UUID.fromString(county))
    }
  }

  private def buildBoxes(list: List[UserProfile]): List[BrowseBox] = {
    list.map {
      userProfile: UserProfile =>
        BrowseBox(
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
    }
  }

}


