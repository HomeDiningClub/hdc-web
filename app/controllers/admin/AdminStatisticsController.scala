package controllers.admin

import enums.{FileTypeEnums, RoleEnums}
import models.viewmodels.StatisticsData
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.{Controller => SpringController}
import play.api.mvc.Controller
import services.{RatingService, ContentFileService, UserCredentialService, RecipeService}
import utils.authorization.WithRole
import securesocial.core.SecureSocial

// Object just needs a default constructor
class AdminStatisticsController extends Controller with SecureSocial {}

@SpringController
object AdminStatisticsController extends Controller with SecureSocial {

  @Autowired
  private var recipeService: RecipeService = _

  @Autowired
  private var ratingService: RatingService = _

  @Autowired
  private var userCredentialService: UserCredentialService = _

  @Autowired
  private var fileService: ContentFileService = _


  def getStatBox = {

    val currentStats = StatisticsData(
      imagesTotal = fileService.getCountOfAllType(FileTypeEnums.IMAGE.toString),
      recipesTotal = recipeService.getCountOfAll,
      membersTotal = userCredentialService.getCountOfAll,
      ratingsRecipeTotal = ratingService.getCountOfAllRecipesRatings,
      ratingsMemberTotal = ratingService.getCountOfAllMemberRatings
    )

    views.html.admin.statistics.stat.render(Some(currentStats))
  }

}