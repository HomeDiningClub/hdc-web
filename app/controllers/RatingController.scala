package controllers

import java.util.UUID
import javax.inject.{Named, Inject}
import constants.FlashMsgConstants
import enums.RoleEnums
import models.{Recipe, UserCredential}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.{Controller => SpringController}
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi, Messages}
import play.api.mvc.{AnyContent, RequestHeader, Controller}
import play.twirl.api.Html
import securesocial.core.SecureSocial
import securesocial.core.SecureSocial.SecuredRequest
import services.{RecipeService, RatingService, UserCredentialService}
import customUtils.authorization.WithRole
import scala.collection.JavaConverters._
import customUtils.security.SecureSocialRuntimeEnvironment
import models.formdata.RatingForm

class RatingController @Inject() (override implicit val env: SecureSocialRuntimeEnvironment,
                                  val messagesApi: MessagesApi,
                                  val userCredentialService: UserCredentialService,
                                  val recipeService: RecipeService,
                                  val ratingService: RatingService) extends Controller with SecureSocial with I18nSupport {

  /*
  @Autowired
  private var userCredentialService : UserCredentialService = _

  @Autowired
  private var recipeService : RecipeService = _

  @Autowired
  private var ratingService : RatingService = _
*/

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

  def renderRecipeRateForm(recipeToBeRated: Recipe, ratingReferrer: String = "/", currentUser: Option[UserCredential])(implicit request: RequestHeader): Html = {

    currentUser match {
      case None =>
        views.html.rating.rateNotLoggedIn()
      case Some(cu) =>

        // Disallow user to rate themselves
        if(ratingService.doesUserTryToRateHimself(cu,recipeToBeRated)){
          views.html.rating.rateErrorMsg.render(Messages("rating.add.give-rate-to-self"), "info", request2Messages)
        }else{

          var ratedBeforeDate: Option[java.util.Date] = None
          val ratingType = "recipe"

          // Set old values if user ranked before
          val formValues = ratingService.hasUserRatedThisBefore(cu, recipeToBeRated) match {
            case None =>
              RatingForm.apply(recipeToBeRated.objectId.toString, 0, None, Some(ratingReferrer), ratingType)
            case Some(relation) =>
              ratedBeforeDate = Some(relation.getLastModifiedDate)
              RatingForm.apply(recipeToBeRated.objectId.toString, relation.ratingValue, Some(relation.ratingComment), Some(ratingReferrer), ratingType)
          }

          views.html.rating.rateRecipe.render(ratingForm.fill(formValues), Some(recipeToBeRated), ratedBeforeDate, Some(cu), request2Messages)
        }
    }
  }


  def renderUserRateForm(userToBeRated: UserCredential, ratingReferrer: String = "/", currentUser: Option[UserCredential])(implicit request: RequestHeader) = {

    currentUser match {
      case None =>
        views.html.rating.rateNotLoggedIn()
      case Some(cu) =>

        // Disallow user to rate themselves
        if(ratingService.doesUserTryToRateHimself(cu,userToBeRated)){
          views.html.rating.rateErrorMsg.render(Messages("rating.add.give-rate-to-self"), "info", request2Messages)
        }else{

          var ratedBeforeDate: Option[java.util.Date] = None
          val ratingType = "user"

          // Set old values if user ranked before
          val formValues = ratingService.hasUserRatedThisBefore(cu, userToBeRated) match {
            case None =>
              RatingForm.apply(userToBeRated.objectId.toString, 0, None, Some(ratingReferrer), ratingType)
            case Some(relation) =>
              ratedBeforeDate = Some(relation.getLastModifiedDate)
              RatingForm.apply(userToBeRated.objectId.toString, relation.ratingValue, Some(relation.ratingComment), Some(ratingReferrer), ratingType)
          }

          views.html.rating.rateUserCred.render(ratingForm.fill(formValues), Some(userToBeRated), ratedBeforeDate, Some(cu), request2Messages)
        }
    }
  }

  def rateSubmit = SecuredAction(authorize = WithRole(RoleEnums.USER))(parse.anyContent) { implicit request: SecuredRequest[AnyContent,UserCredential] =>
    val currentUser = request.user

    ratingForm.bindFromRequest.fold(
      errors => {
        request.headers.get("Referer") match {
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
                BadRequest(views.html.rating.rateErrorMsg.render(Messages("rating.add.error"), "error", request2Messages))
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
                BadRequest(views.html.rating.rateErrorMsg.render(Messages("rating.add.error"), "error", request2Messages))
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
