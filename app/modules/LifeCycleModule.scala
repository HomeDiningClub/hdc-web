package modules

import play.api.{Logger, Configuration, Environment}
import play.api.inject._
import services._
import traits._
class LifeCycleModule extends Module {

  def bindings(environment: Environment, configuration: Configuration) = {
    Logger.debug("Binding LifeCycleModule")
    Seq(
      bind[ApplicationStopService].to[ApplicationStopServiceImpl].eagerly(),
      bind[ApplicationStartService].to[ApplicationStartServiceImpl].eagerly()
    )
  }
}
