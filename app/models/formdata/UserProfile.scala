package models.formdata

import play.data.validation.Constraints.Required


/**
 * 2014-04-12, 16:57
 */
case class UserProfile
(
  userName     : String,
  emailAddress : String,
  firstName    : String,
  lastName     : String,
  aboutMe      : String,
  quality: List[Boolean],
  county       : String,
  idNo         : Long
) {
  var id:Int = UserProfile.nextId
}

// http://www.fdmtech.org/2012/03/a-better-example-of-play-framework-2-0-with-mybatis-for-scala-beta/


object  UserProfile {
  private var currentId = 0


  def nextId: Int = {
    currentId += 1
    currentId
  }




}

