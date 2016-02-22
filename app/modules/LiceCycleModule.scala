package modules

import com.google.inject.AbstractModule
import play.api.{Configuration, Environment}
import play.api.inject._
import services.LifeCycleComponentImpl
import traits.LifeCycleComponent

class LiceCycleModule extends Module  {
/*
  def configure() = {
  }
*/

  def bindings(environment: Environment, configuration: Configuration) = {
      Seq(
        bind[LifeCycleComponent].to[LifeCycleComponentImpl]
      )
  }
}
