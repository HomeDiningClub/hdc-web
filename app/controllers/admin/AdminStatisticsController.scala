package controllers.admin

import javax.inject.{Named, Inject}

import enums.{FileTypeEnums, RoleEnums}
import models.viewmodels.StatisticsData
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.{Controller => SpringController}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{RequestHeader, Controller}
import services.{RatingService, ContentFileService, UserCredentialService, RecipeService}
import customUtils.authorization.WithRole
import models.UserCredential
import customUtils.security.SecureSocialRuntimeEnvironment

//@Named
class AdminStatisticsController @Inject() (override implicit val env: SecureSocialRuntimeEnvironment, val messagesApi: MessagesApi) extends Controller with securesocial.core.SecureSocial with I18nSupport {

  @Autowired
  private var recipeService: RecipeService = _

  @Autowired
  private var ratingService: RatingService = _

  @Autowired
  private var userCredentialService: UserCredentialService = _

  @Autowired
  private var fileService: ContentFileService = _

  def getStatBox: StatisticsData = {

    val currentStats = StatisticsData(
      imagesTotal = fileService.getCountOfAllType(FileTypeEnums.IMAGE.toString),
      recipesTotal = recipeService.getCountOfAll,
      membersTotal = userCredentialService.getCountOfAll,
      ratingsRecipeTotal = ratingService.getCountOfAllRecipesRatings,
      ratingsMemberTotal = ratingService.getCountOfAllMemberRatings
    )

    currentStats

    //views.html.admin.statistics.stat.render(Some(currentStats), request2Messages)
  }

}