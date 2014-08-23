package utils.requests

import play.api.mvc.RequestHeader

class NormalizedRequest(request: RequestHeader) extends RequestHeader {

  val id = request.id
  val headers = request.headers
  val queryString = request.queryString
  val remoteAddress = request.remoteAddress
  val method = request.method
  val version = request.version
  val tags = request.tags


  val path = if(request.path.length > 1) { request.path.stripSuffix("/") }else{ request.path }
  val uri = path + {
    if(request.rawQueryString == "") ""
    else "?" + request.rawQueryString
  }
}

object NormalizedRequest {
  def apply(request: RequestHeader) = new NormalizedRequest(request)
}