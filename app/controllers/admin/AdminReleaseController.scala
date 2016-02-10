package controllers.admin

import javax.inject.{Named, Inject}

import org.springframework.stereotype.{Controller => SpringController}
import play.api.mvc._

import org.springframework.beans.factory.annotation.Autowired
import securesocial.core.SecureSocial
import securesocial.core.SecureSocial.SecuredRequest
import services._
import play.api.i18n.{I18nSupport, MessagesApi, Messages}
import constants.FlashMsgConstants
import enums.RoleEnums
import models.UserCredential
import customUtils.authorization.WithRole
import customUtils.security.SecureSocialRuntimeEnvironment

class AdminReleaseController @Inject() (override implicit val env: SecureSocialRuntimeEnvironment,
                                        val countyService: CountyService,
                                        val tagWordService: TagWordService,
                                        val userRoleService: UserRoleService,
                                        val userCredentialService: UserCredentialService,
                                        val messagesApi: MessagesApi) extends Controller with SecureSocial with I18nSupport {
/*
  @Autowired
  private var countyService: CountyService = _

  @Autowired
  private var tagWordService: TagWordService = _

  @Autowired
  private var userRoleService: UserRoleService = _

  @Autowired
  private var userCredentialService: UserCredentialService = _
*/

  def editIndex() = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request: SecuredRequest[AnyContent,UserCredential] =>
    Ok(views.html.admin.release.index())
  }

  // Create default data in this method
  def createDefaultData = UserAwareAction { implicit request =>

    // Clear all tags
    tagWordService.deleteAll

    // Create default Tags
    val groupName = "profile"
    tagWordService.createTag("Amerikanskt", "Amerikanskt", "quality[0]", groupName)
    tagWordService.createTag("Italienskt", "Italienskt", "quality[1]", groupName)
    tagWordService.createTag("Franskt", "Franskt", "quality[2]", groupName)
    tagWordService.createTag("Asiatiskt", "Asiatiskt", "quality[3]", groupName)
    tagWordService.createTag("Svensk husman", "Svensk husman", "quality[4]", groupName)
    tagWordService.createTag("Mellanöstern", "Mellanöstern", "quality[5]", groupName)
    tagWordService.createTag("Vegetarisk", "Vegetarisk", "quality[6]", groupName)
    tagWordService.createTag("RAW-food", "RAW-food", "quality[7]", groupName)
    tagWordService.createTag("LCHF", "LCHF", "quality[8]", groupName)
    tagWordService.createTag("Koscher", "Koscher", "quality[9]", groupName)
    tagWordService.createTag("Vilt", "Vilt", "quality[10]", groupName)
    tagWordService.createTag("Kött", "Kött", "quality[11]", groupName)
    tagWordService.createTag("Fisk och skaldjur", "Fisk och skaldjur", "quality[12]", groupName)
    tagWordService.createTag("Lyx", "Lyx", "quality[13]", groupName)
    tagWordService.createTag("Budget", "Budget", "quality[14]", groupName)
    tagWordService.createTag("Barnvänligt", "Barnvänligt", "quality[15]", groupName)
    tagWordService.createTag("Friluftsmat", "Friluftsmat", "quality[16]", groupName)
    tagWordService.createTag("Drycker", "Drycker", "quality[17]", groupName)
    tagWordService.createTag("Efterrätter", "Efterrätter", "quality[18]", groupName)
    tagWordService.createTag("Bakverk", "Bakverk", "quality[19]", groupName)

    // Clear all countys
    countyService.deleteAll

    // Create county's
    countyService.createCounty("Blekinge", 1, true)
    countyService.createCounty("Dalarna", 2, true)
    countyService.createCounty("Gotland", 3, true)
    countyService.createCounty("Gävleborg", 4, true)
    countyService.createCounty("Halland", 5, true)
    countyService.createCounty("Jämtland", 6, true)
    countyService.createCounty("Jönköping", 7, true)
    countyService.createCounty("Kalmar", 8, true)
    countyService.createCounty("Kronoberg", 9, true)
    countyService.createCounty("Norrbotten", 10, true)
    countyService.createCounty("Stockholm", 11, true)
    countyService.createCounty("Södermanland", 12, true)
    countyService.createCounty("Uppsala", 13, true)
    countyService.createCounty("Värmland", 14, true)
    countyService.createCounty("Västerbotten", 15, true)
    countyService.createCounty("Västernorrland", 16, true)
    countyService.createCounty("Västmanland", 17, true)
    countyService.createCounty("Västra Götaland", 18, true)
    countyService.createCounty("Örebro", 19, true)
    countyService.createCounty("Östergötland", 20, true)

    // Clear all roles
    userRoleService.deleteAll

    userRoleService.createRole(RoleEnums.ADMIN)
    userRoleService.createRole(RoleEnums.POWERUSER)
    userRoleService.createRole(RoleEnums.USER)


    Ok("Default data created/recreated")
  }

  def addAdmin = UserAwareAction { implicit request =>

    // Add running user to admin group
    request.user match {
      case Some(reqUser) =>
        val currentUser = userCredentialService.findById(reqUser.asInstanceOf[UserCredential].objectId)
        if(currentUser != null) {
          userCredentialService.addRole(currentUser.get, RoleEnums.ADMIN)
        }
      case None =>
    }

    Ok("User added to admin-role")
  }

}