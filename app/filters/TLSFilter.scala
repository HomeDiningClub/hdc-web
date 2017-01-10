package filters

import javax.inject.Inject
import play.api.mvc.Result
import scala.concurrent._
import ExecutionContext.Implicits.global
import play.api.mvc.Filter
import play.api.mvc._
import scala.concurrent.Future
import play.api.http._

class TLSFilter @Inject()(implicit app: play.api.Application) extends Filter {

  def apply(nextFilter: RequestHeader => Future[Result])
           (requestHeader: RequestHeader): Future[Result] = {

    if(app.mode == play.api.Mode.Prod){
      if (!requestHeader.secure)
        Future.successful(Results.MovedPermanently("https://" + requestHeader.host + requestHeader.uri))
      else
        nextFilter(requestHeader).map(_.withHeaders("Strict-Transport-Security" -> "max-age=31536000; includeSubDomains"))
    }else{
      nextFilter(requestHeader)
    }
  }

}

class MyFilters @Inject()(implicit app: play.api.Application) extends HttpFilters {
  val filters = Seq(new TLSFilter)
}