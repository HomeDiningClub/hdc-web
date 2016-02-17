package modules

import com.typesafe.config.ConfigFactory
import org.neo4j.graphdb.GraphDatabaseService
import org.springframework.beans.factory.DisposableBean
import org.springframework.context.annotation._
import org.springframework.data.neo4j.config.{EnableNeo4jRepositories}
import org.springframework.data.neo4j.rest.SpringCypherRestGraphDatabase
import org.springframework.transaction.annotation.EnableTransactionManagement
import play.api.Logger
import javax.inject.Singleton

@EnableTransactionManagement(mode = AdviceMode.ASPECTJ)
@Configuration
@EnableNeo4jRepositories(basePackages = Array("repositories"))
@ComponentScan(Array("repositories", "models"))
@Singleton
class SpringNeo4jRestConfig extends SpringNeo4jBaseConfig with DisposableBean {

  private val host = ConfigFactory.load().getString("neo4j.host")
  private val user = ConfigFactory.load().getString("neo4j.user")
  private val password = ConfigFactory.load().getString("neo4j.password")
  private val database = new SpringCypherRestGraphDatabase(host, user, password)

  @Bean
  def graphDatabaseService(): GraphDatabaseService = {

    if (Logger.isDebugEnabled) {
      Logger.debug("Connecting to remote database: " + user + "@" + host)
    }
    database
  }

  @throws(classOf[Exception])
  override def destroy() = {
    if (Logger.isDebugEnabled) {
      Logger.debug("Shutting down DB..")
    }

    database.shutdown()

    if (Logger.isDebugEnabled) {
      Logger.debug("...done")
    }
  }

}