package controllers

import javax.inject._

import play.api.{Environment, Logger}
import play.api.http.HttpErrorHandler
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.RequestHeader

import scala.concurrent.Future
import play.api.mvc.Results._

/**
 * This is an alternative to ErrorController, if we show too much information in production
 * Activate by changing in application.config: play.http.errorHandler = "controllers.ErrorProdController"
 */
class ErrorProdController @Inject()(val messagesApi: MessagesApi,
                                    val environment: Environment) extends HttpErrorHandler with I18nSupport {


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
    Future.successful(InternalServerError(views.html.error.error(ex = currentEx, isAdmin = false)(req, request2Messages)))
  }

}
