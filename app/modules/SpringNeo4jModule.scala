package modules

import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.guice.module.SpringModule
import javax.inject.Singleton

/**
 * The spring module, which will be passed to spring-guice
 * https://github.com/unterstein/play-2.4-scala-spring-data-sample/blob/master/app/modules/SpringNeo4jModule.scala
 * @author Johannes Unterstein (unterstein@me.com)
 */

@Singleton
class SpringNeo4jModule extends SpringModule(new AnnotationConfigApplicationContext(classOf[SpringNeo4jEmbeddedConfig])) {

}
