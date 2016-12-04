package models.message

import java.util

import org.springframework.data.neo4j.annotation.{QueryResult, ResultColumn}

@QueryResult
trait MessageData {

  @ResultColumn("owner.")
  def getprofileLinkName() : String

  @ResultColumn("recipient")
  def getEventImage() : util.List[String]

  @ResultColumn("MainImage")
  def getMainImage() : util.List[String]

  @ResultColumn("CountyName")
  def getCountyName() : String

  @ResultColumn("TagName")
  def getTagName() : String

  @ResultColumn("UserImage")
  def getUserImage() : util.List[String]
}

