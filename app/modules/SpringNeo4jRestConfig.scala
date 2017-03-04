package modules

import com.typesafe.config.ConfigFactory
import org.neo4j.graphdb.GraphDatabaseService
import org.springframework.beans.factory.DisposableBean
import org.springframework.context.annotation._
import org.springframework.data.auditing.config.AuditingConfiguration
import org.springframework.data.neo4j.config.{Neo4jAuditingBeanDefinitionParser, EnableNeo4jRepositories}
import org.springframework.data.neo4j.rest.SpringCypherRestGraphDatabase
import org.springframework.transaction.annotation.EnableTransactionManagement
import javax.inject.Singleton
import javax.inject._

import play.api.inject.ApplicationLifecycle

@EnableTransactionManagement(mode = AdviceMode.ASPECTJ)
@Configuration
@EnableNeo4jRepositories(basePackages = Array("repositories"))
@ComponentScan(Array("repositories", "models"))
@Singleton
class SpringNeo4jRestConfig extends SpringNeo4jBaseConfig with DisposableBean {

  private val host = ConfigFactory.load().getString("neo4j.host")
  private val user = ConfigFactory.load().getString("neo4j.user")
  private val password = ConfigFactory.load().getString("neo4j.password")

  @Bean
  def graphDatabaseService(): GraphDatabaseService = {
    if(database == null){
      doLog("Creating new connection to database: " + user + "@" + host)
      database = new SpringCypherRestGraphDatabase(host, user, password)
    }
    doLog("Connecting to existing remote database: " + user + "@" + host)
    database
  }

  // There is also a LifeCycleService to kill the database connection between resets, this is just a fallback
  @throws(classOf[Exception])
  override def destroy(): Unit = {
    shutDownDb()
  }

}