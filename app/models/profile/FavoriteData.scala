package models.profile

import java.util

import org.springframework.data.neo4j.annotation.{QueryResult, ResultColumn}

@QueryResult
trait FavoriteData {

  @ResultColumn("UserCredentialObjectId")
  def getUserCredentialObjectId() : String

  @ResultColumn("UserProfileObjectId")
  def getUserProfileObjectId() : String

  @ResultColumn("FirstName")
  def getFirstName() : String

  @ResultColumn("LastName")
  def getLastName() : String

  @ResultColumn("AboutMeHeadline")
  def getAboutMeHeadline(): String

  @ResultColumn("ProfileLinkName")
  def getProfileLinkName() : String

  @ResultColumn("AvatarImage")
  def getAvatarImage() : util.List[String]
}

