package models.rating

import java.util
import org.springframework.data.neo4j.annotation.{QueryResult, ResultColumn}

@QueryResult
trait ReviewData {

  @ResultColumn("ReviewObjectId")
  def getObjectId() : String

  @ResultColumn("UserWhoIsRatingFirstName")
  def getUserWhoIsRatingFirstName() : String

  @ResultColumn("UserWhoIsRatingProfileLinkName")
  def getUserWhoIsRatingProfileLinkName() : String

  @ResultColumn("RatedProfileLinkName")
  def getRatedProfileLinkName() : String

  @ResultColumn("NameOfRatedItem")
  def getNameOfRatedItem() : String

  @ResultColumn("LinkToRatedItem")
  def getLinkToRatedItem() : String

  @ResultColumn("ReviewText")
  def getReviewText() : String

  @ResultColumn("LastModifiedDate")
  def getLastModifiedDate() : util.Date

  @ResultColumn("UserWhoIsRatingAvatarImage")
  def getUserWhoIsRatingAvatarImage() : util.List[String]

  @ResultColumn("RatingValue")
  def getRatingValue() : Int

}

