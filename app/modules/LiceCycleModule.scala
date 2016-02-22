package modules

import play.api.{Logger, Configuration, Environment}
import play.api.inject._
import services.LifeCycleServiceImpl
import traits.LifeCycleService

class LiceCycleModule extends Module {

  def bindings(environment: Environment, configuration: Configuration) = {
    Logger.debug("Binding LifeCycleModule")
    Seq(bind[LifeCycleService].to[LifeCycleServiceImpl].eagerly())
  }
}
