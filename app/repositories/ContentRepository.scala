package repositories

import org.springframework.data.neo4j.repository.GraphRepository
import models.content._
import org.springframework.data.neo4j.annotation.Query

trait ContentRepository extends GraphRepository[ContentBase]{

//  Doesn't work, need a manual mapper:
//  template.createEntityFrom[Stored]State(userNode[,User.class)

//  @Query("MATCH (pages:`ContentPage`) RETURN pages")
//  def getContentPages(): Array[ContentPage]

}