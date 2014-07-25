package repositories

import org.springframework.data.neo4j.repository.GraphRepository
import models.content._
import org.springframework.data.neo4j.annotation.Query
import models.{UserRole, Recipe}
import java.util.UUID

trait UserRoleRepository extends GraphRepository[UserRole] {

  // Auto-mapped by Spring
  def findByobjectId(objectId: UUID): UserRole
  def findByname(name: String): UserRole

}