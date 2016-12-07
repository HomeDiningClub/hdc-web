package repositories

import org.springframework.data.neo4j.repository.GraphRepository
import models.content._
import org.springframework.data.neo4j.annotation.Query
import models.ViewedByMember
import java.util.UUID

trait ViewedByMemberRepository extends GraphRepository[ViewedByMember] {

  @Query("match (v:ViewedByMember)-[:IN_USER_VISITED]-(p:UserProfile {objectId:{0}}) RETURN length(v.userAccessLog)")
  def countViewsByMember(userProfileObjectId: String) : Int

}
