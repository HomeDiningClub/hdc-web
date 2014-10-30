package repositories

import org.springframework.data.neo4j.repository.GraphRepository
import models.content._
import org.springframework.data.neo4j.annotation.Query
import models.{UserRole, Recipe}
import java.util.UUID

trait UserRoleRepository extends GraphRepository[UserRole] {

  @Query("MATCH (n:`UserRole`) WHERE n.objectId={0} RETURN n")
  def findByobjectId(objectId: UUID): UserRole

  @Query("MATCH (n:`UserRole`) WHERE n.name={0} RETURN n")
  def findByname(name: String): UserRole

}