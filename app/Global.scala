import play.api._
import com.tinkerpop.blueprints.Vertex
import com.wingnest.play2.frames.GraphDB

object Global extends GlobalSettings {

  private var init: Boolean = false

  override def onStart(app: Application) {
    Logger.info("Application has started")

    try {
      if (!init) {
        //GraphDB.createKeyIndex("className", classOf[Nothing])
        init = true
      }
    }
    catch {
      case e: UnsupportedOperationException => Logger.info(e.getMessage)
    }
  }

  override def onStop(app: Application) {
    Logger.info("Application shutdown...")
  }

}