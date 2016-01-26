package global

import modules.SpringNeo4jRestConfig
import play.api.routing.Router
import play.api.ApplicationLoader.Context
import play.api.BuiltInComponentsFromContext
import play.api.i18n.I18nComponents
import play.api.i18n._
import play.api._
import traits.ProvidesAppContext

import scala.concurrent.Future

/*
class ApplicationComponents(context: Context) extends BuiltInComponentsFromContext(context) with I18nComponents {
  //lazy val logService = new LogService
  lazy val dbService = new SpringNeo4jRestConfig()
  //lazy val applicationController = new controllers.Application(linkService)
  //lazy val appContext = new MyComponent(messagesApi)

  applicationLifecycle.addStopHook(() => Future.successful(dbService.destroy()))

  lazy val assets = new controllers.Assets(httpErrorHandler)
  override lazy val router = Router.empty
  //val myFilter = new MyFilter(configuration)
  //override lazy val httpFilters = Seq(myFilter)
}
*/