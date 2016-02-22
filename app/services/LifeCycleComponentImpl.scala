package services

import javax.inject.Inject
import play.api.Logger
import play.api.inject.ApplicationLifecycle
import traits.LifeCycleComponent

import scala.concurrent.Future

class LifeCycleComponentImpl @Inject()(lifecycle: ApplicationLifecycle) extends LifeCycleComponent {

  lifecycle.addStopHook { () =>
    Logger.debug("Lifecycle stop hook")
    Future.successful(())
  }
}
