import play.api.mvc.Handler
import play.api.Application
import play.api.GlobalSettings
import play.api.Logger
import org.springframework.context.support.ClassPathXmlApplicationContext
import play.api.mvc._
import play.api.mvc.Results._
import scala.concurrent.Future

// Java2Scala
// http://javatoscala.com/

//object Global {
//  private var init: Boolean = false
//}

object Global extends GlobalSettings {
  /**
   * Declare the application context to be used.
   */

  val ctx = new ClassPathXmlApplicationContext("applicationContext.xml")

  /**
   * Sync the context lifecycle with Play's.
   * @param app
   */
  override def onStart(app: Application) {
    Logger.info("Application has started")
    // Needed for embedded DB
    //ctx.start()
  }

  /**
   * Sync the context lifecycle with Play's.
   * @param app
   */
  override def onStop(app: Application) {
    Logger.info("Application shutdown...")

    // Needed for embedded DB
    //val neoTemplate:Neo4jTemplate = ctx.getBean(classOf[Neo4jTemplate])
    //neoTemplate.getGraphDatabaseService.shutdown()
    //ctx.stop()

    //ctx.close() - May or may not be needed
  }

//  override def onRouteRequest(request: play.mvc.Http.RequestHeader): Option[Handler] = {
//    Some(super.onRouteRequest(NormalizedRequest(request)))
//  }

  override def onError(request: RequestHeader, ex: Throwable) = {
    Future.successful(InternalServerError(views.html.error.error(ex = ex)(request)))
  }

  override def onHandlerNotFound(request: RequestHeader) = {
    Future.successful(NotFound(views.html.error.notfound(refUrl = request.path)(request)))
  }

  override def onBadRequest(request: RequestHeader, error: String) = {
    Future.successful(InternalServerError(views.html.error.error(errorString = error)(request)))
  }

  /**
   * Controllers must be resolved through the application context. There is a special method of GlobalSettings
   * that we can override to resolve a given controller. This resolution is required by the Play router.
   * @param controllerClass
   * @param A
   * @return
   */
  override def getControllerInstance[A](controllerClass: Class[A]): A = ctx.getBean(controllerClass)
}