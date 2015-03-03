package repositories

import org.springframework.data.neo4j.repository.GraphRepository
import models.content._
import org.springframework.data.neo4j.annotation.Query
import models.ViewedByMember
import java.util.UUID

trait ViewdByMemberRepository extends GraphRepository[ViewedByMember] {

}
