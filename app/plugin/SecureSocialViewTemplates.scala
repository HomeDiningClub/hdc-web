package plugin

import javax.inject.Inject

import customUtils.security.SecureSocialRuntimeEnvironment
import play.api.mvc.{RequestHeader, Request}
import play.api.data.Form
import play.twirl.api.{Txt, Html}
import play.api.i18n.{Messages, I18nSupport, MessagesApi, Lang}
import securesocial.controllers.{MailTemplates, ViewTemplates}
import securesocial.core.{SecureSocial, BasicProfile}

import play.api.Play.current
import play.api.i18n.Messages.Implicits._

class SecureSocialViewTemplates @Inject() (implicit val env: SecureSocialRuntimeEnvironment) extends ViewTemplates {

  /**
   * Returns the html for the login page
   * @param request
   * @return
   */
  override def getLoginPage(form: Form[(String, String)], msg: Option[String] = None)(implicit request: RequestHeader, lang: Lang): Html =
  {
    views.html.custom.login(form, msg)
  }

  /**
   * Returns the html for the signup page
   *
   * @param request
   * @return
   */
  override def getSignUpPage(form: Form[securesocial.controllers.RegistrationInfo], token: String)(implicit request: RequestHeader, lang: Lang): Html = {
    views.html.custom.Registration.signUp(form, token)
  }

  /**
   * Returns the html for the start signup page
   *
   * @param request
   * @return
   */
  override def getStartSignUpPage(form: Form[String])(implicit request: RequestHeader, lang: Lang): Html = {
    views.html.custom.Registration.startSignUp(form)
  }

  /**
   * Returns the html for the reset password page
   *
   * @param request
   * @return
   */
  override def getStartResetPasswordPage(form: Form[String])(implicit request: RequestHeader, lang: Lang): Html = {
    views.html.custom.Registration.startResetPassword(form)
  }

  /**
   * Returns the html for the start reset page
   *
   * @param request
   * @return
   */
  override def getResetPasswordPage(form: Form[(String, String)], token: String)(implicit request: RequestHeader, lang: Lang): Html = {
    views.html.custom.Registration.resetPasswordPage(form, token)
  }

  /**
   * Returns the html for the change password page
   *
   * @param request
   * @param form
   * @return
   */
  override def getPasswordChangePage(form: Form[securesocial.controllers.ChangeInfo])(implicit request: RequestHeader, lang: Lang): Html = {
    views.html.custom.passwordChange(form)
  }

  override def getNotAuthorizedPage(implicit request: RequestHeader, lang: Lang): Html = {
    views.html.custom.notAuthorized()
  }
}