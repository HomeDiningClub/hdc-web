package modules

import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.guice.module.SpringModule
import javax.inject.Singleton

@Singleton
class SpringNeo4jModule extends SpringModule(new AnnotationConfigApplicationContext(classOf[SpringNeo4jEmbeddedConfig])) {

}
