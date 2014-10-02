package controllers

import java.util.UUID
import constants.FlashMsgConstants
import enums.RoleEnums
import models.{Recipe, UserCredential}
import models.viewmodels.RatingForm
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.{Controller => SpringController}
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages
import play.api.mvc.{SimpleResult, RequestHeader, Controller}
import play.api.templates.Html
import securesocial.core.SecureSocial
import services.{RecipeService, RatingService, UserCredentialService}
import utils.authorization.WithRole
import scala.collection.JavaConverters._

// Object just needs a default constructor
class RatingController extends Controller { }

@SpringController
object RatingController extends Controller with SecureSocial {

  @Autowired
  private var userCredentialService : UserCredentialService = _

  @Autowired
  private var recipeService : RecipeService = _

  @Autowired
  private var ratingService : RatingService = _

  // Rating form mapping
  val ratingForm = Form(
    mapping(
      "id" -> text,
      "ratingValue" -> number(),
      "ratingComment" -> optional(text),
      "ratingReferrer" -> optional(text),
      "ratingType" -> text
    )(RatingForm.apply)(RatingForm.unapply)
  )

  def renderRecipeRateForm(recipeToBeRated: Recipe, ratingReferrer: String = "/") = { implicit request: RequestHeader =>

    utils.Helpers.getUserFromRequest match {
      case None =>
        views.html.rating.rateNotLoggedIn()
      case Some(currentUser) =>

        // Disallow user to rate themselves
        if(ratingService.doesUserTryToRateHimself(currentUser,recipeToBeRated)){
          views.html.rating.rateErrorMsg.render(Messages("rating.add.give-rate-to-self"), "info")
        }else{

          var ratedBeforeDate: Option[java.util.Date] = None
          val ratingType = "recipe"

          // Set old values if user ranked before
          val formValues = ratingService.hasUserRatedThisBefore(currentUser, recipeToBeRated) match {
            case None =>
              RatingForm.apply(recipeToBeRated.objectId.toString, 0, None, Some(ratingReferrer), ratingType)
            case Some(relation) =>
              ratedBeforeDate = Some(relation.getLastModifiedDate)
              RatingForm.apply(recipeToBeRated.objectId.toString, relation.ratingValue, Some(relation.ratingComment), Some(ratingReferrer), ratingType)
          }

          views.html.rating.rateRecipe.render(ratingForm.fill(formValues), Some(recipeToBeRated), ratedBeforeDate, request)
        }
    }
  }


  def renderUserRateForm(userToBeRated: UserCredential, ratingReferrer: String = "/") = { implicit request: RequestHeader =>

    utils.Helpers.getUserFromRequest match {
      case None =>
        views.html.rating.rateNotLoggedIn()
      case Some(currentUser) =>

        // Disallow user to rate themselves
        if(ratingService.doesUserTryToRateHimself(currentUser,userToBeRated)){
          views.html.rating.rateErrorMsg.render(Messages("rating.add.give-rate-to-self"), "info")
        }else{

          var ratedBeforeDate: Option[java.util.Date] = None
          val ratingType = "user"

          // Set old values if user ranked before
          val formValues = ratingService.hasUserRatedThisBefore(currentUser, userToBeRated) match {
            case None =>
              RatingForm.apply(userToBeRated.objectId.toString, 0, None, Some(ratingReferrer), ratingType)
            case Some(relation) =>
              ratedBeforeDate = Some(relation.getLastModifiedDate)
              RatingForm.apply(userToBeRated.objectId.toString, relation.ratingValue, Some(relation.ratingComment), Some(ratingReferrer), ratingType)
          }

          views.html.rating.rateUserCred.render(ratingForm.fill(formValues), Some(userToBeRated), ratedBeforeDate, request)
        }
    }
  }

  def rateSubmit = SecuredAction(authorize = WithRole(RoleEnums.USER))(parse.anyContent) { implicit request =>
    val currentUser = utils.Helpers.getUserFromRequest.get

    ratingForm.bindFromRequest.fold(
      errors => {
        request.headers.get("referrer") match {
          case Some(referrerUrl) =>
            Redirect(referrerUrl, 302).flashing(FlashMsgConstants.Error -> Messages("rating.add.error"))
          case None =>
            Redirect(routes.UserProfileController.viewProfileByName(currentUser.profiles.asScala.head.profileLinkName)).flashing(FlashMsgConstants.Error -> Messages("rating.add.error"))
        }
      },
      formContent => {

        formContent.ratingType match {
          case "user" =>
            userCredentialService.findById(UUID.fromString(formContent.id)) match {
              case None =>
                BadRequest(views.html.rating.rateErrorMsg.render(Messages("rating.add.error"), "error"))
              case Some(userToBeRated) => {
                    ratingService.rateUser(
                      currentUser,
                      userToBeRated,
                      formContent.ratingValue,
                      formContent.ratingComment.getOrElse(""),
                      request.remoteAddress)
                }

                Redirect(routes.UserProfileController.viewProfileByName(userToBeRated.profiles.asScala.head.profileLinkName))
              }
          case "recipe" =>
            recipeService.findById(UUID.fromString(formContent.id)) match {
              case None =>
                BadRequest(views.html.rating.rateErrorMsg.render(Messages("rating.add.error"), "error"))
              case Some(recipeToBeRated) => {
                ratingService.rateRecipe(
                  currentUser,
                  recipeToBeRated,
                  formContent.ratingValue,
                  formContent.ratingComment.getOrElse(""),
                  request.remoteAddress)
              }
                Redirect(routes.RecipePageController.viewRecipeByNameAndProfile(recipeToBeRated.getOwnerProfile.profileLinkName, recipeToBeRated.getLink))
            }
          case _ =>
            Logger.error("Cannot accept post without ratingType")
            sys.error("Error in request")

        }
      }
    )
  }
}
