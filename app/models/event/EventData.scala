package models.event

import java.util
import org.springframework.data.neo4j.annotation.{ResultColumn, QueryResult}

@QueryResult
trait EventData {

  @ResultColumn("ProfileLinkName")
  def getprofileLinkName() : String

  @ResultColumn("EventLinkName")
  def getLinkName() : String

  @ResultColumn("EventName")
  def getName() : String

  @ResultColumn("EventPrice")
  def getPrice() : java.lang.Long

  @ResultColumn("EventObjectId")
  def getobjectId() : String

  @ResultColumn("EventPreAmble")
  def getpreAmble() : String

  @ResultColumn("EventMainBody")
  def getMainBody() : String

  @ResultColumn("EventImages")
  def getEventImage() : util.List[String]

  @ResultColumn("MainImage")
  def getMainImage() : util.List[String]

  @ResultColumn("CountyName")
  def getCountyName() : String

  @ResultColumn("TagName")
  def getTagName() : String
}

