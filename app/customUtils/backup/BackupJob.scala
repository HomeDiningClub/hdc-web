package customUtils.backup

import akka.actor.ActorSystem
import play.api.Play.current
import play.api.libs.concurrent.Akka
import play.api.{Configuration, Mode, Environment, Logger}
import java.util.Calendar
import java.text.SimpleDateFormat
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object BackupJob {

  def startJob(environment: Environment, configuration: Configuration, actorSystem: ActorSystem ) = {

    // Every 12 hours store data from the register to file
    // Only in production

    if(environment.mode.equals(Mode.Prod)) {
      actorSystem.scheduler.schedule(12.hour, 12.hour) {

        Logger.info("Backup started ... ")
        customUtils.backup.BackupData.makeFullBackup(configuration)
        val today = Calendar.getInstance().getTime()

        val dateFormat = new SimpleDateFormat("HH:mm:ss.SS")
        Logger.info("Backup done : " + dateFormat.format(today))
      }
    }

  }

}
