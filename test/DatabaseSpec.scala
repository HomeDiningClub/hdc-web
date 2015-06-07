import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import org.springframework.beans.factory.annotation.Autowired
import play.api.Logger

import play.api.test._
import play.api.test.Helpers._

import services._

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
@RunWith(classOf[JUnitRunner])
class DatabaseSpec extends Specification {


  "Database indexes" should {

    "userprofileIndex" in new WithApplication {

      var startTime = System.currentTimeMillis
      var results = InstancedServices.userProfileService.findByprofileLinkName("testname")
      var endTime = System.currentTimeMillis
      var requestTime = endTime - startTime

      Logger.info("findByprofileLinkName:" + requestTime)


      //recipeService.findByrecipeLinkName(recipeName, fetchAll = true) match {

    }
  }
}
