package controllers

import play.api.Logger
import play.api.http.HttpErrorHandler
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.RequestHeader
import play.api.mvc.Results._
import scala.concurrent.Future
import javax.inject._

class ErrorController @Inject()(val messagesApi: MessagesApi) extends HttpErrorHandler with I18nSupport {

  // 4xx
  def onClientError(request: RequestHeader, statusCode: Int, message: String) = {
    implicit val req = request

    Logger.info(s"Returning $statusCode for: ${request.uri}")

    if(statusCode == play.api.http.Status.NOT_FOUND){
      Future.successful(NotFound(views.html.error.notfound(refUrl = request.path)(req, request2Messages)))
    }else if(statusCode == play.api.http.Status.BAD_REQUEST){
      Future.successful(InternalServerError(views.html.error.error(errorString = message)))
    }else{
      Future.successful(Status(statusCode)(views.html.error.error(errorString = message)))
    }
  }

  // 5xx
  def onServerError(request: RequestHeader, exception: Throwable) = {
    implicit val req = request
    var currentEx = exception

    /*
    val isAdmin: Boolean = Helpers.isUserAdmin(request.user)

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
    */
    Future.successful(InternalServerError(views.html.error.error(ex = currentEx, isAdmin = false)(req, request2Messages)))
  }

}
