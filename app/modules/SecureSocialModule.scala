package modules

import com.google.inject.{AbstractModule, TypeLiteral}
import customUtils.security.SecureSocialRuntimeEnvironment
import net.codingwell.scalaguice.ScalaModule
import play.api.i18n.{MessagesApi, I18nSupport}
import securesocial.core.RuntimeEnvironment
import services.ContentService


class SecureSocialModule extends AbstractModule with ScalaModule {
  override def configure() {
    val environment: SecureSocialRuntimeEnvironment = new SecureSocialRuntimeEnvironment()
    bind(new TypeLiteral[RuntimeEnvironment] {}).toInstance(environment)
  }
}

