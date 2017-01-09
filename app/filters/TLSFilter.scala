package filters


import play.api.mvc.Result

import scala.concurrent._
import ExecutionContext.Implicits.global

import play.api.mvc.Filter

import play.api._
import play.api.mvc._
import play.filters.headers.SecurityHeadersFilter

import scala.concurrent.Future

import play.api.http._

//import meta._
//import Assets._

class TLSFilter extends Filter {
  def apply(nextFilter: RequestHeader => Future[Result])
           (requestHeader: RequestHeader): Future[Result] = {
    if(!requestHeader.secure)
      Future.successful(Results.MovedPermanently("https://" + requestHeader.host + requestHeader.uri))
    else
      nextFilter(requestHeader).map(_.withHeaders("Strict-Transport-Security" -> "max-age=31536000; includeSubDomains"))
  }
}

class MyFilters extends HttpFilters {
  val filters = Seq(new TLSFilter)
}