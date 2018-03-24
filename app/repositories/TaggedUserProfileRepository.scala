package repositories

import java.util

import models.profile.{TagWord, TaggedUserProfile}
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.repository.GraphRepository


trait TaggedUserProfileRepository extends GraphRepository[TaggedUserProfile] {

  @Query("MATCH (up:`UserProfile`{objectId:{0}})-[tup:`TAGGED_ON`]->(tw:`TagWord`{tagGroupName:{1}}) RETURN tw.tagName")
  def findTagWordsTaggedByUserProfileReturnName(userProfileObjectId: String, groupName: String): util.List[String]

  @Query("MATCH (up:`UserProfile`{objectId:{0}})-[tup:`TAGGED_ON`]->(tw:`TagWord`{tagGroupName:{1}}) RETURN tw")
  def findTagWordsTaggedByUserProfileReturnTagWord(userProfileObjectId: String, groupName: String): util.List[TagWord]
}
