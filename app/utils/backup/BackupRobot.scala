package utils.backup

import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar


/*
import akka.actor._
import scala.concurrent.duration._

import play.api._
import play.api.libs.json._
import play.api.libs.iteratee._
import play.api.libs.concurrent._

import akka.util.Timeout
import akka.pattern.ask

import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._
*/
object BackupRobot {

  var v : Int = 0


  def main(args: Array[String]) {
    println("Hello, world!")


    var dir: File = new File("C:\\UTV\\KATALOG")


    var files = dir.listFiles().iterator

    while(files.hasNext) {
      var item = files.next()
      if(item.isDirectory) {
        var cal : Calendar = java.util.Calendar.getInstance()
        var saveForPeriod : Calendar = java.util.Calendar.getInstance()
        saveForPeriod.add(Calendar.DATE, - 7)
        cal.setTimeInMillis(item.lastModified())
        var format : SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd")
        println("Datum :  " + format.format(cal.getTimeInMillis))
        println("comp Datum :  " + format.format(saveForPeriod.getTimeInMillis))
        if(saveForPeriod.getTimeInMillis > cal.getTimeInMillis) {

          var removed = item.delete()
          println ("ta bort folder. OK :  " + removed)
        } else {
          println("spara. OK ")
        }
        println("DIR : " + item.getName)
      }
    }


    var separator = java.io.File.separator
    println("File separator : " + separator)

    println("MegaByte : " + freeSpace("c:\\", 20))

  }

  def freeSpace(folder: String, megaByte : Integer) : Boolean = {

    val toMegaByte : Long =  (1024 * 1024)

    // todo use paramter value
    var dir : File  = new File(folder)

    var freeSpace   : Long = dir.getFreeSpace / toMegaByte
    var usableSpace : Long = dir.getUsableSpace / toMegaByte

    println("freeSpace : " + freeSpace)
    println("usableSpace : " + usableSpace)

    usableSpace > megaByte
  }



  def doFile {
    val toMegaByte: Long = (1024 * 1024)

    // todo use paramter value
    var dir: File = new File("C:\\UTV\\KATALOG")
    var freeSpace: Long = dir.getFreeSpace / toMegaByte
    var usableSpace: Long = dir.getUsableSpace / toMegaByte

    val today = Calendar.getInstance().getTime()
    val dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSS")
    val nowString = dateFormat.format(today)
    println(nowString)
    println("FREE_SPACE : " + freeSpace)
    var dirDir: File = new File("C:\\UTV\\KATALOG\\" + nowString)
    var successful: Boolean = dirDir.mkdir()
    if (successful) {
      println("OK")
    } else {
      println("FEL")
    }
  }
}
