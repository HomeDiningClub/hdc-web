package services

import javax.inject._
import org.springframework.data.neo4j.support.Neo4jTemplate
import play.api.Logger
import play.api.inject.ApplicationLifecycle
import play.api.libs.concurrent.Akka
import traits.ApplicationStopService
import scala.concurrent.Future

@Singleton
class ApplicationStopServiceImpl @Inject()(lifecycle: ApplicationLifecycle, template: Neo4jTemplate) extends ApplicationStopService {

  initialize()

  def initialize() {
    lifecycle.addStopHook { () =>
      Logger.debug("Application stop")
      stopDatabase()
      Future.successful(())
    }
  }

  private def stopDatabase(){
    Logger.debug("Shutting down database..")

    if(template != null){
      template.getGraphDatabaseService.shutdown()
      Logger.debug("Database shutdown - Done")
    }else{
      Logger.debug("Database is null, cannot shut down")
    }


  }
}
