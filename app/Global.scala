import java.text.SimpleDateFormat
import java.util.Calendar

import controllers.routes
import enums.RoleEnums
import org.springframework.data.neo4j.support.Neo4jTemplate
import play.api.libs.concurrent.Akka
import play.api.mvc.Handler
import play.api.Application
import play.api.GlobalSettings
import play.api.Logger
import org.springframework.context.support.ClassPathXmlApplicationContext
import play.api.mvc._
import play.api.mvc.Results._
import scala.concurrent.Future
import utils.authorization.WithRole
import utils.Helpers
import utils.requests.NormalizedRequest
import utils.filters.GlobalLoggingFilter
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.duration._
import play.api.Play.current
/*
import play.filters.gzip.GzipFilter
import play.filters.csrf.CSRFFilter
*/
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
    ctx.start()

    // every 12 houre store data from the register to file
    Akka.system.scheduler.schedule(12.hour, 12.hour) {
      println("Backup started ... ")
      utils.backup.BackupData.makeFullBackup()
     val today = Calendar.getInstance().getTime()
      val dateFormat = new SimpleDateFormat("HH:mm:ss.SS")
      println("Backup done : " + dateFormat.format(today))
    }


  }


  /**
   * Sync the context lifecycle with Play's.
   * @param app
   */
  override def onStop(app: Application) {
    Logger.info("Application shutdown...")

    //Needed for embedded DB
    val neoTemplate:Neo4jTemplate = ctx.getBean(classOf[Neo4jTemplate])
    neoTemplate.getGraphDatabaseService.shutdown()
    ctx.stop()
    ctx.close() //- May or may not be needed
  }

  // Disable filter for now
//  override def doFilter(next: EssentialAction): EssentialAction = {
//    Filters(super.doFilter(next), GlobalLoggingFilter)//, new GzipFilter())//, CSRFFilter())
//  }

  override def onError(request: RequestHeader, ex: Throwable) = {
    var currentEx = ex

    val isAdmin: Boolean = Helpers.isUserAdmin(request)

    if(isAdmin){
      var foundRootCause: Boolean = false
      var loopStopper = 100
      do {
        if (currentEx.getCause != null) {
          currentEx = currentEx.getCause
          loopStopper -= 1
        } else {
          foundRootCause = true
        }
      } while (!foundRootCause || loopStopper == 0)
    }

    Future.successful(InternalServerError(views.html.error.error(ex = currentEx, isAdmin = isAdmin)(request)))
  }


  override def onHandlerNotFound(request: RequestHeader) = {
    Future.successful(NotFound(views.html.error.notfound(refUrl = request.path)(request)))
  }

  override def onBadRequest(request: RequestHeader, error: String) = {
    Future.successful(InternalServerError(views.html.error.error(errorString = error)(request)))
  }

  override def onRouteRequest(request: RequestHeader): Option[Handler] = {

    // Is Production?
    if(play.api.Play.isProd(play.api.Play.current)){
      val approvedPaths = List("/assets/","/login", "/logout", "/signup", "/reset", "/password", "/authenticate", "/not-authorized")

      // Checked for logged in user
      if(utils.Helpers.getUserFromRequest(request).isEmpty){

          // Anonymous
          if(!approvedPaths.exists(path => request.path.contains(path))){
              return Some(controllers.CampaignController.index)
          }
      }
    }

    // Remove trailing slash
    super.onRouteRequest(NormalizedRequest(request))
  }

  /**
   * Controllers must be resolved through the application context. There is a special method of GlobalSettings
   * that we can override to resolve a given controller. This resolution is required by the Play router.
   * @param controllerClass
   * @return
   */
  override def getControllerInstance[A](controllerClass: Class[A]): A = ctx.getBean(controllerClass)
}