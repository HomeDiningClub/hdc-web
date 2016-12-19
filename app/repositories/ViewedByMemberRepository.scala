package repositories

import org.springframework.data.neo4j.repository.GraphRepository
import models.content._
import org.springframework.data.neo4j.annotation.Query
import models.ViewedByMember
import java.util.UUID

trait ViewedByMemberRepository extends GraphRepository[ViewedByMember] {

  @Query("optional match (v:ViewedByMember)-[:IN_USER_VISITED]-(p:UserProfile {objectId:{0}}) WITH CASE WHEN v IS NULL THEN 0 ELSE length(v.userAccessLog) END As CountValue RETURN CountValue")
  def countViewsByMember(userProfileObjectId: String) : Int

}
