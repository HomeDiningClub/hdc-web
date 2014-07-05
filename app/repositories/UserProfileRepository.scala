package repositories

import org.springframework.data.neo4j.repository.GraphRepository
import models.UserProfile
import org.springframework.data.neo4j.annotation.Query


trait  UserProfileRepository extends GraphRepository[UserProfile] {


}
