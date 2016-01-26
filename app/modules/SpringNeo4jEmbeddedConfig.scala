package modules

import javax.inject.Inject

import com.typesafe.config.ConfigFactory
import org.apache.commons.lang.StringUtils
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.graphdb.factory.GraphDatabaseFactory
import org.springframework.beans.factory.DisposableBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.neo4j.config.{Neo4jConfiguration, EnableNeo4jRepositories}
import org.springframework.transaction.annotation.EnableTransactionManagement
import java.io.File
import play.api.Logger
import play.api.inject.ApplicationLifecycle

import scala.concurrent.Future

@EnableTransactionManagement
@Configuration
@EnableNeo4jRepositories(basePackages = Array("repositories"))
@ComponentScan(Array("models", "repositories"))
class SpringNeo4jEmbeddedConfig extends SpringNeo4jBaseConfig with DisposableBean {

  private val embeddedDB = ConfigFactory.load().getString("neo4j.embeddedDB")
  private var database: GraphDatabaseService = null

  @Bean
  def graphDatabaseService(): GraphDatabaseService = {
    if (StringUtils.isEmpty(embeddedDB)) {
      throw new RuntimeException("Could not find config for embedded DB: " + embeddedDB)
    }

    doLog("Connecting to embedded database: " + embeddedDB)

    val graphDatabaseFactory: GraphDatabaseFactory = new GraphDatabaseFactory(){}
    val embeddedStore: File = new File(embeddedDB)

    database = graphDatabaseFactory.newEmbeddedDatabase(embeddedStore)

//    lifecycle.addStopHook { () =>
//      Future.successful(destroy())
//    }

    database
  }

  @throws(classOf[Exception])
  override def destroy() = {
    doLog("Shutting down DB..")

    if(database != null){
      database.shutdown()
      doLog("..done")
    }else{
      doLog("Database is null, cannot shut down")
    }

  }

  private def doLog(message: String): Unit = {
    if (Logger.isDebugEnabled) {
      Logger.debug(message)
    }
  }
}
