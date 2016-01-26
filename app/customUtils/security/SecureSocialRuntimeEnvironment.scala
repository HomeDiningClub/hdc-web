package customUtils.security

import play.api.i18n.MessagesApi
import securesocial.core.RuntimeEnvironment
import models.UserCredential
import services.{LoginEventListener, UserCredentialServicePlugin}
import scala.collection.immutable.ListMap
import securesocial.controllers.{MailTemplates, ViewTemplates}
import plugin.{SecureSocialMailTemplates, SecureSocialViewTemplates}
import javax.inject.Inject

class SecureSocialRuntimeEnvironment extends RuntimeEnvironment.Default {

  type U = UserCredential
  override implicit val executionContext = play.api.libs.concurrent.Execution.defaultContext
  override lazy val viewTemplates: ViewTemplates = new SecureSocialViewTemplates()(this)
  override lazy val mailTemplates: MailTemplates = new SecureSocialMailTemplates()(this)
  override lazy val userService: UserCredentialServicePlugin = new UserCredentialServicePlugin
  override lazy val eventListeners = List(new LoginEventListener())
  override lazy val providers = ListMap(
    /*
    include(new FacebookProvider(routes, cacheService, oauth2ClientFor(FacebookProvider.Facebook))),
    include(new securesocial.core.providers.GitHubProvider(routes, cacheService, oauth2ClientFor(GitHubProvider.GitHub))),
    include(new securesocial.core.providers.GoogleProvider(routes, cacheService, oauth2ClientFor(GoogleProvider.Google))),
    include(new securesocial.core.providers.LinkedInProvider(routes, cacheService, oauth1ClientFor(LinkedInProvider.LinkedIn))),
    include(new securesocial.core.providers.TwitterProvider(routes, cacheService, oauth1ClientFor(TwitterProvider.Twitter))),
    */
    include(new securesocial.core.providers.UsernamePasswordProvider[UserCredential](userService, avatarService, viewTemplates, passwordHashers))
  )
}
