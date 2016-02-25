package services

import akka.actor.ActorSystem
import play.api.{Configuration, Environment, Logger}
import traits.ApplicationStartService
import javax.inject._

@Singleton
class ApplicationStartServiceImpl @Inject() (environment: Environment,
                                             configuration: Configuration,
                                             actorSystem: ActorSystem) extends ApplicationStartService {

  initialize()

  def initialize() {
    Logger.debug("Application start")
    startDbBackup()
  }

  private def startDbBackup(){
    Logger.debug("Starting backup service...")
    customUtils.backup.BackupJob.startJob(environment, configuration, actorSystem)
  }

}
