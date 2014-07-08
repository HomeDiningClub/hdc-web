package repositories

import models.profile.TagWord
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.repository.GraphRepository


trait TagWordRepository extends GraphRepository[TagWord] {

  @Query("start up=node:TagWord(tagGroupName={0}) return up")
  def getByGroupName(tagGroupName: String ):  Array[TagWord]

}
