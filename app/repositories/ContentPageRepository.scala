package repositories

import org.springframework.data.neo4j.repository.GraphRepository
import models.content._
import org.springframework.data.neo4j.annotation.Query
import java.util.UUID

trait ContentPageRepository extends GraphRepository[ContentPage] {

  // Auto-mapped by Spring
  def findByobjectId(objectId: UUID): ContentPage
  def findByRouteAndContentState(route: String, contentState: String): ContentPage

  @Query("MATCH (n:`ContentPage`) WHERE n.route = {0} AND HAS(n.contentCategories) AND {1} IN n.contentCategories AND n.visibleInMenus = {2} AND n.contentState = {3} RETURN n")
  def findByRouteAndContentCategoriesAndVisibleInMenusAndContentState(route: String, contentCategory: String, visibleInMenus: Boolean, contentState: String): ContentPage

  @Query("MATCH (n:`ContentPage`) WHERE HAS(n.contentCategories) AND {0} IN n.contentCategories AND n.contentState = {1} RETURN n")
  def findByContentCategoriesAndContentState(contentCategory: String, contentState: String): java.util.List[ContentPage]

  @Query("MATCH (n:`ContentPage`) WHERE HAS(n.contentCategories) AND {0} IN n.contentCategories AND n.visibleInMenus = {1} AND n.contentState = {2} RETURN n")
  def findByContentCategoriesAndVisibleInMenusAndContentState(contentCategory: String, visibleInMenus: Boolean, contentState: String): java.util.List[ContentPage]

  @Query("MATCH (n:`ContentPage`) WHERE HAS(n.contentCategories) AND {0} IN n.contentCategories RETURN n")
  def findByContentCategories(contentCategory: String): java.util.List[ContentPage]

}