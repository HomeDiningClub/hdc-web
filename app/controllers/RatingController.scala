package controllers

import java.util.UUID
import enums.RoleEnums
import models.UserCredential
import models.viewmodels.RatingForm
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.{Controller => SpringController}
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages
import play.api.mvc.{SimpleResult, RequestHeader, Controller}
import play.api.templates.Html
import securesocial.core.SecureSocial
import services.{MailService, RatingService, UserCredentialService}
import utils.authorization.WithRole
import scala.collection.JavaConverters._
import java.text.DateFormat

// Object just needs a default constructor
class RatingController extends Controller { }

@SpringController
object RatingController extends Controller with SecureSocial {

  @Autowired
  private var userCredentialService : UserCredentialService = _

  @Autowired
  private var ratingService : RatingService = _

  // Edit - Add Content
  val ratingForm = Form(
    mapping(
      "id" -> text,
      "ratingValue" -> number(),
      "ratingComment" -> optional(text),
      "ratingReferrer" -> optional(text)
    )(RatingForm.apply)(RatingForm.unapply)
  )

  def renderRateForm(userToBeRated: UserCredential, ratingReferrer: String = "/") = { implicit request: RequestHeader =>

    utils.Helpers.getUserFromRequest match {
      case None =>
        views.html.rating.rateNotLoggedIn()
      case Some(currentUser) =>

        // Disallow user to rate themselves
        if(userToBeRated.objectId.toString.equalsIgnoreCase(currentUser.objectId.toString)){
          views.html.rating.rateErrorMsg.render(Messages("rating.add.give-rate-to-self"), "info")
        }else{

          var ratedBeforeDate: Option[java.util.Date] = None

          // Set old values if user ranked before
          val formValues = ratingService.hasUserRatedThisUser(currentUser, userToBeRated) match {
            case None =>
              RatingForm.apply(userToBeRated.objectId.toString, 0, None, Some(ratingReferrer))
            case Some(relation) =>
              ratedBeforeDate = Some(relation.getLastModifiedDate)
              RatingForm.apply(userToBeRated.objectId.toString, relation.ratingValue, Some(relation.ratingComment), Some(ratingReferrer))
          }

          views.html.rating.rateUserCred.render(ratingForm.fill(formValues), Some(userToBeRated), ratedBeforeDate, request)
        }
    }
  }

  def rateUserSubmit = SecuredAction(authorize = WithRole(RoleEnums.USER))(parse.anyContent) { implicit request =>
    val currentUser = utils.Helpers.getUserFromRequest.get

    ratingForm.bindFromRequest.fold(
      errors => {
        BadRequest(views.html.rating.rateErrorMsg.render(Messages("rating.add.error"), "error"))
      },
      formContent => {
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

            Redirect(routes.UserProfileController.viewProfileByName(userToBeRated.profiles.asScala.head.profileLinkName))
          }
        }
      }
    )
  }
}
