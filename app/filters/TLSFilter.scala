package filters

import javax.inject.Inject

import akka.stream.Materializer
import play.api.mvc.Result

import scala.concurrent._
import play.api.mvc.Filter
import play.api.mvc._

import scala.concurrent.Future
import play.api.http._

class TLSFilter @Inject()(implicit val mat: Materializer, implicit val ec: ExecutionContext, implicit val env: play.api.Environment) extends Filter {

  def apply(nextFilter: RequestHeader => Future[Result])
           (requestHeader: RequestHeader): Future[Result] = {
    if(env.mode == play.api.Mode.Prod){
      if (!requestHeader.secure)
        Future.successful(Results.MovedPermanently("https://" + requestHeader.host + requestHeader.uri))
      else
        nextFilter(requestHeader).map(_.withHeaders("Strict-Transport-Security" -> "max-age=31536000; includeSubDomains"))
    }else{
      nextFilter(requestHeader)
    }
  }

}

class MyFilters @Inject()(implicit val mat: Materializer, implicit val ec: ExecutionContext, implicit val env: play.api.Environment) extends HttpFilters {
  val filters = Seq(new TLSFilter())
}