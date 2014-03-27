import play.api._
import com.tinkerpop.blueprints.Vertex
import com.wingnest.play2.frames.GraphDB
import play.Logger


object Global extends GlobalSettings {

  private var init: Boolean = false

  override def onStart(app: Application) {
    //Logger.info("Application has started")

    try {
      if (!init) {
       //GraphDB.createKeyIndex("className", Vertex)
        init = true
      }
    }
  //  catch {
      //case e: UnsupportedOperationException => Logger.info("test")
  //  }

  }

  override def onStop(app: Application) {
    //Logger.info("Application shutdown...")
  }

}