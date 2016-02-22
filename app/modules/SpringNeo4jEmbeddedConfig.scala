package modules

import com.typesafe.config.ConfigFactory
import org.apache.commons.lang.StringUtils
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.graphdb.factory.GraphDatabaseFactory
import org.springframework.beans.factory.DisposableBean
import org.springframework.context.annotation._
import org.springframework.data.neo4j.config.EnableNeo4jRepositories
import org.springframework.transaction.annotation.EnableTransactionManagement
import java.io.File
import javax.inject.{Inject, Singleton}

import play.api.inject.ApplicationLifecycle

@EnableTransactionManagement(mode = AdviceMode.ASPECTJ)
@Configuration
@EnableNeo4jRepositories(basePackages = Array("repositories"))
@ComponentScan(Array("repositories", "models"))
@Singleton
class SpringNeo4jEmbeddedConfig extends SpringNeo4jBaseConfig with DisposableBean {

  private val embeddedDB = ConfigFactory.load().getString("neo4j.embeddedDB")

  @Bean
  def graphDatabaseService(): GraphDatabaseService = {
    if(database == null){
      if (StringUtils.isEmpty(embeddedDB)) {
        throw new RuntimeException("Could not find config for embedded DB: " + embeddedDB)
      }

      doLog("Creating new connection to embedded database: " + embeddedDB)
      val graphDatabaseFactory: GraphDatabaseFactory = new GraphDatabaseFactory(){}
      val embeddedStore: File = new File(embeddedDB)

      database = graphDatabaseFactory.newEmbeddedDatabase(embeddedStore)
    }

    doLog("Connecting to existing: " + embeddedDB)
    database
  }

  // There is also a LifeCycleService to kill the database connection between resets, this is just a fallback
  @throws(classOf[Exception])
  override def destroy() = {
    shutDownDb()
  }

}
