package repositories

import org.springframework.data.neo4j.repository.GraphRepository
import models.content._
import java.util.UUID
import org.springframework.data.neo4j.annotation.Query

trait RelatedPageRepository extends GraphRepository[RelatedPage] {

  // Auto-mapped by Spring
  def findByobjectId(objectId: UUID): RelatedPage
  def findByrelatedFrom(contentPage: ContentPage): java.util.List[RelatedPage]
  def findByrelatedFromObjectId(objectId: UUID): java.util.List[RelatedPage]
  def findByrelatedTo(contentPage: ContentPage): java.util.List[RelatedPage]
  def findByrelatedToObjectId(objectId: UUID): java.util.List[RelatedPage]

  @Query("MATCH (fromPage)-[relation:`RELATED_PAGE`]->(toPage) WHERE fromPage.objectId={0} OR toPage.objectId={0} RETURN relation")
  def findByrelatedFromOrRelatedToObjectId(objectId: UUID): java.util.List[RelatedPage]
}