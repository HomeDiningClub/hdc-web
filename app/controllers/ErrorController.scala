package controllers

import play.api.routing.Router
import play.api._
import play.api.http.{DefaultHttpErrorHandler, HttpErrorHandler}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{RequestHeader, Result}
import play.api.mvc.Results._

import scala.concurrent.Future
import javax.inject._


class ErrorController @Inject() (val messagesApi: MessagesApi,
                               env: Environment,
                               config: Configuration,
                               sourceMapper: OptionalSourceMapper,
                               router: Provider[Router],
                                 val environment: Environment) extends DefaultHttpErrorHandler(env, config, sourceMapper, router) with I18nSupport {

  override def onProdServerError(request: RequestHeader, exception: UsefulException): Future[Result] = {
    implicit val req = request
    Logger.info(s"500 Server error: ${request.uri}")
    Future.successful(InternalServerError(views.html.error.error(ex = exception, isAdmin = false)(req,request2Messages)))
  }

  override def onBadRequest(request: RequestHeader, message: String): Future[Result] = {
    implicit val req = request
    Logger.info(s"400 Bad request: ${request.uri}")
    Future.successful(InternalServerError(views.html.error.error(errorString = message)(req,request2Messages)))
  }

  override def onNotFound(request: RequestHeader, message: String): Future[Result] = {
    implicit val req = request
    Logger.info(s"404 Not Found: ${request.uri}")
    Future.successful(NotFound(views.html.error.notfound(refUrl = request.path)(req,request2Messages)))
  }
}

/*

*/