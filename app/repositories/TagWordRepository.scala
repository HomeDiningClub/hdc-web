package repositories

import models.profile.TagWord
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.repository.GraphRepository
import java.util.UUID
import java.util


trait TagWordRepository extends GraphRepository[TagWord] {

  @Query("MATCH (n:`TagWord`) WHERE n.objectId={0} RETURN n")
  def findByobjectId(objectId: UUID): TagWord

  // Auto-mapped by Spring
  def findByTagGroupName(tagGroupName: String): util.List[TagWord]

  @Query("MATCH (n:`TagWord`) WHERE n.objectId IN {0} RETURN n")
  def findByListOfobjectId(objectIdList: Array[String]): util.List[TagWord]

  @Query("start up=node:TagWord(tagGroupName={0}) return up")
  def getByGroupName2(tagGroupName: String ): Array[TagWord]

}
