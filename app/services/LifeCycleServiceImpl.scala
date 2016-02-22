package services

import javax.inject.Inject
import org.springframework.data.neo4j.support.Neo4jTemplate
import play.api.Logger
import play.api.inject.ApplicationLifecycle
import traits.LifeCycleService

import scala.concurrent.Future

class LifeCycleServiceImpl @Inject()(lifecycle: ApplicationLifecycle, template: Neo4jTemplate) extends LifeCycleService {

  initialize()

  def initialize() {
    lifecycle.addStopHook { () =>
      Logger.debug("Lifecycle: Stopping application")
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
