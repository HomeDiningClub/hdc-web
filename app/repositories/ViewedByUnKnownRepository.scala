package repositories

import org.springframework.data.neo4j.repository.GraphRepository
import models.content._
import org.springframework.data.neo4j.annotation.Query
import models.ViewedByUnKnown
import java.util.UUID

trait ViewedByUnKnownRepository extends GraphRepository[ViewedByUnKnown] {

  @Query("match (v:ViewedByUnKnown)-[:IN_OTHER_VISITED]-(p:UserProfile {objectId:{0}}) RETURN length(v.userAccessLog)")
  def countViewsByUnknown(userProfileObjectId: String) : Int

}
