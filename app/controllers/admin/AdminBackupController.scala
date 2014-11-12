package controllers.admin

import org.springframework.stereotype.{Controller => SpringController}
import play.api.mvc.Controller
import securesocial.core.SecureSocial
import play.api.data.Form
import play.api.data.Forms._
import models.viewmodels.TagWordForm
import org.springframework.beans.factory.annotation.Autowired
import services.TagWordService
import play.api.i18n.Messages
import constants.FlashMsgConstants
import models.profile.TagWord
import java.util.UUID
import utils.authorization.WithRole
import enums.RoleEnums

@SpringController
class AdminBackupController extends Controller with SecureSocial {

  def listAllBackupJobs = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request =>

    Ok(views.html.admin.backup.index())
  }

  def doBackup = SecuredAction(authorize = WithRole(RoleEnums.ADMIN)) { implicit request =>

    utils.backup.BackupData.makeFullBackup()

    Ok("Done!")
  }



}
