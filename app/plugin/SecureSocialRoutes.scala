package plugin

import play.api.Configuration
import play.api.http.HttpConfiguration
import play.api.mvc.RequestHeader
import securesocial.core.services.RoutesService

class SecureSocialRoutes(configuration: Configuration, httpConfiguration: HttpConfiguration) extends RoutesService.Default(configuration) {
  val applicationHostKey: String =  configuration.getString(ApplicationHostKey).getOrElse {
    throw new RuntimeException(s"Missing property: $ApplicationHostKey")
  }
}
