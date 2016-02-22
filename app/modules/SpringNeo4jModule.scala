package modules

import com.google.inject.Inject
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.guice.module.SpringModule
import javax.inject.Singleton

import play.api.inject.{ApplicationLifecycle, Binding, Module}
import play.api.{Logger, Configuration, Environment}

import scala.concurrent.Future

/**
 * The spring module, which will be passed to spring-guice
 * https://github.com/unterstein/play-2.4-scala-spring-data-sample/blob/master/app/modules/SpringNeo4jModule.scala
 * @author Johannes Unterstein (unterstein@me.com)
 */

@Singleton
class SpringNeo4jModule extends SpringModule(new AnnotationConfigApplicationContext(classOf[SpringNeo4jEmbeddedConfig])) {

  //val springModule: SpringModule = new SpringModule(new AnnotationConfigApplicationContext(classOf[SpringNeo4jEmbeddedConfig]))
  /*
  init()

  def init() {
      applicationLifecycle.addStopHook { () =>
      Future.successful {
        //springContext.stop()
        Logger.debug("Lifecycle stop hook - SpringNeo4jModule")
        //shutDownDb()
      }
    }
  }
*/


/*
  def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = {
    Seq(play.api.inject.bind[SpringNeo4jEmbeddedConfig].toSelf.eagerly)
  }
  */
}
