package models

import java.util
import java.util.UUID

import org.springframework.data.neo4j.annotation.{QueryResult, ResultColumn}

@QueryResult
trait UserProfileData {

  @ResultColumn("UserProfileObjectId")
  def getUserProfileObjectId() : UUID

  @ResultColumn("UserProfileLinkName")
  def getProfileLinkName() : String

  @ResultColumn("County")
  def getCounty() : String

  @ResultColumn("MainImage")
  def getMainImage() : util.List[String]

  @ResultColumn("AvatarImage")
  def getAvatarImage() : util.List[String]

  @ResultColumn("UserAverageRating")
  def getUserAverageRating() : Int

  @ResultColumn("UserProfileRoles")
  def getUserProfileRoles() : String

}

