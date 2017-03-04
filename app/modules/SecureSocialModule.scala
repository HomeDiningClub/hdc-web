package modules

import customUtils.security.SecureSocialRuntimeEnvironment
import play.api.{ Configuration, Environment }
import play.api.inject.{ Binding, Module }
import securesocial.core.RuntimeEnvironment

class SecureSocialModule extends Module {
  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = {
    Seq(
      bind[RuntimeEnvironment].to[SecureSocialRuntimeEnvironment]
    )
  }
}

