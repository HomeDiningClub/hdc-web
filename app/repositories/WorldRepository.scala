package repositories

import org.springframework.data.neo4j.repository.GraphRepository
import models.World



trait WorldRepository extends GraphRepository[World]{

}