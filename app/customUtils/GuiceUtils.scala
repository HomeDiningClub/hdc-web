package customUtils

import play.api.inject.guice.GuiceApplicationBuilder

import scala.reflect.ClassTag

// Usage:
// trait Secured {
// val authService = customUtils.GuiceUtils.inject[AuthService]
// ...
//}
object GuiceUtils {
  lazy val injector = new GuiceApplicationBuilder().injector()
  def inject[T: ClassTag]: T = injector.instanceOf[T]
}
