package controllers

import org.springframework.stereotype.{Controller => SpringController}
import play.api.mvc._
import securesocial.core.{SecuredRequest, SecureSocial}
import models.{UserCredential, Recipe}
import play.api.data.Form
import play.api.data.Forms._
import scala.Some
import models.viewmodels.RecipeForm
import play.api.i18n.Messages
import constants.FlashMsgConstants
import org.springframework.beans.factory.annotation.Autowired
import services.{UserCredentialService, ContentFileService, RecipeService}
import play.api.libs.Files.TemporaryFile
import enums.{RoleEnums, FileTypeEnums}
import java.util.UUID
import presets.ImagePreSets
import utils.authorization.WithRole
import utils.authorization.WithRole
import scala.Some
import models.viewmodels.RecipeForm
import play.api.mvc.Security.AuthenticatedRequest
import play.api.libs.Files

@SpringController
class RecipePageController extends Controller with SecureSocial {

  @Autowired
  private var recipeService: RecipeService = _

  @Autowired
  private var fileService: ContentFileService = _


  def index() = UserAwareAction { implicit request =>
    Ok(views.html.recipe.recipe())
  }

}