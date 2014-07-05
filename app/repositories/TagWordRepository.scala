package repositories

import models.profile.TagWord
import org.springframework.data.neo4j.repository.GraphRepository

trait TagWordRepository extends GraphRepository[TagWord] {

}
