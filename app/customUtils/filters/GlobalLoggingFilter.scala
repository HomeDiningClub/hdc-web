package customUtils.filters

import javax.inject.Inject

import akka.stream.Materializer
import play.api.mvc.{Filter, RequestHeader, Result}
import play.api.Logger

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.routing.Router

class GlobalLoggingFilter @Inject()(implicit val mat: Materializer) extends Filter {
  def apply(nextFilter: (RequestHeader) => Future[Result])(requestHeader: RequestHeader): Future[Result] = {

    val startTime = System.currentTimeMillis

    nextFilter(requestHeader).map { result =>

      val action = requestHeader.tags(Router.Tags.RouteController) +
        "." + requestHeader.tags(Router.Tags.RouteActionMethod) +
        "-" + requestHeader.uri

      val endTime = System.currentTimeMillis
      val requestTime = endTime - startTime

      Logger.info(s"${action} took ${requestTime}ms and returned ${result.header.status}")

      result.withHeaders("Request-Time" -> requestTime.toString)
    }
  }
}
