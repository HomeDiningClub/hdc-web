import org.specs2.mutable._
import customUtils.ViewedByMemberUtil

class HelloWorldSpec extends Specification {

  "The 'Hello world' string" should {
    "contain 11 characters" in {
      "Hello world" must have size(11)
    }
    "start with 'Hello'" in {
      "Hello world" must startWith("Hello")
    }
    "end with 'world'" in {
      "Hello world" must endWith("world")
    }
  }

  "getName should get name" should {
    "should be equal to test" in {
      val util = new ViewedByMemberUtil()
      var str : String = "test,2014-10-10"
      var name : String = util.getNamne(str)
      name must be equalTo "test2"
    }
  }

  "Now should be" should {
    "now should be equals to" in {
      val util = new ViewedByMemberUtil()
      var str : String = "test,2014-10-10"
      var name : String = util.getNamne(str)
      util.getNowString must be equalTo "2015-03-06"
    }
  }



}
