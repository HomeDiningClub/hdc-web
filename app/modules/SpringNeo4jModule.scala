package modules

import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.guice.module.SpringModule

class SpringNeo4jModule extends SpringModule(new AnnotationConfigApplicationContext(classOf[SpringNeo4jRestConfig])) {

}
