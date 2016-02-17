package modules

import com.typesafe.config.ConfigFactory
import org.apache.commons.lang.StringUtils
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.graphdb.factory.GraphDatabaseFactory
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation._
import org.springframework.data.neo4j.config.{JtaTransactionManagerFactoryBean, EnableNeo4jRepositories}
import org.springframework.data.transaction.ChainedTransactionManager
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import java.io.File
import play.api.Logger
import javax.inject.Singleton


@EnableTransactionManagement(mode = AdviceMode.ASPECTJ)
@Configuration
@EnableNeo4jRepositories(basePackages = Array("repositories"))
@ComponentScan(Array("repositories", "models"))
@Singleton
class SpringNeo4jEmbeddedConfig extends SpringNeo4jBaseConfig with DisposableBean {

  private val embeddedDB = ConfigFactory.load().getString("neo4j.embeddedDB")
  private var database: GraphDatabaseService = null

  @Bean
  def graphDatabaseService(): GraphDatabaseService = {
    if(database == null){
      if (StringUtils.isEmpty(embeddedDB)) {
        throw new RuntimeException("Could not find config for embedded DB: " + embeddedDB)
      }

      doLog("Connecting to embedded database: " + embeddedDB)
      val graphDatabaseFactory: GraphDatabaseFactory = new GraphDatabaseFactory(){}
      val embeddedStore: File = new File(embeddedDB)

      database = graphDatabaseFactory.newEmbeddedDatabase(embeddedStore)
    }
    database
  }

  @throws(classOf[Exception])
  override def destroy() = {
    doLog("Shutting down database")

    if(database != null){
      database.shutdown()
      doLog("Database shutdown - Done")
    }else{
      doLog("Database is null, cannot shut down")
    }

  }


  /*
    @Bean(name = Array("transactionManager"))
    @throws(classOf[Exception])
    override def neo4jTransactionManager(graphDatabaseService: GraphDatabaseService): PlatformTransactionManager = {
      new ChainedTransactionManager(new org.springframework.data.neo4j.config.JtaTransactionManagerFactoryBean(graphDatabaseService).getObject)
    }

    @Bean(name = {"neo4jTransactionManager","transactionManager"})
    @Qualifier("neo4jTransactionManager")
    @DependsOn("graphDatabaseService")
    public PlatformTransactionManager neo4jTransactionManager(GraphDatabaseService graphDatabaseService) {
      JtaTransactionManagerFactoryBean jtaTransactionManagerFactoryBean = new JtaTransactionManagerFactoryBean(graphDatabaseService);
      return jtaTransactionManagerFactoryBean.getObject();
    }

    @Bean
    def transactionManager(): PlatformTransactionManager = {
      val txManager: JtaTransactionManagerFactoryBean = new JtaTransactionManagerFactoryBean(graphDatabaseService())
      txManager.getObject
    }

    @Bean
    def txManager(): PlatformTransactionManager = {
      new DataSourceTransactionManager(database)
    }
  */


  private def doLog(message: String): Unit = {
    if (Logger.isDebugEnabled) {
      Logger.debug(message)
    }
  }
}
