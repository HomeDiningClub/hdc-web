package repositories

import models.profile.TagWord
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.repository.GraphRepository


trait TagWordRepository extends GraphRepository[TagWord] {

  @Query("start up=node:TagWord(tagGroupName={0}) return up")
  def getByGroupName2(tagGroupName: String ):  Array[TagWord]


 // def findByGruoupName(tagGroupName :String ): Iterable[TagWord]
}
