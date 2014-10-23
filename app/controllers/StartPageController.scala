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

  // Search startpage form
  val searchStartPageForm = Form(
    mapping(
      //"freeText" -> optional(text),
      "fCounty" -> optional(text),
      "fTag" -> optional(text)
    )(SearchStartPageForm.apply)(SearchStartPageForm.unapply)
  )

  def index(fTag: String, fCounty: String) = UserAwareAction { implicit request =>


    val startPageBoxes = getStartPageBoxes(fTag, fCounty)
    val form = SearchStartPageForm.apply(
      fCounty match { case null | "" => None case item => Some(item)},
      fTag match { case null | "" => None case item => Some(item)}
    )

    Ok(views.html.startpage.index(
      searchForm = searchStartPageForm.fill(form),
      optionsFoodAreas = getFoodAreas,
      optionsLocationAreas = getCounties,
      startPageBoxes = startPageBoxes,
      reviewBoxes = ratingService.getUserReviewBoxesStartPage(true,8),
      asideNews = contentService.getAsideNewsItems
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


  private def getStartPageBoxes(boxFilterTag: String, boxFilterCounty: String): Option[List[StartPageBox]] = {

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

    val startPageBoxes: Option[List[StartPageBox]] = userProfileService.getUserProfilesFiltered(filterTag = fetchedTag, filterCounty = fetchedCounty) match {
      case None => None
      case Some(profile) => Some(profile.map {
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
              case image => Some(image.getTransformByName("box").getUrl)
            },
            userImage = userProfile.getAvatarImage match {
              case null => None
              case image => Some(image.getTransformByName("thumbnail").getUrl)
            },
            userRating = userProfile.getOwner.getAverageRating)
      })
    }
    startPageBoxes
  }



}


