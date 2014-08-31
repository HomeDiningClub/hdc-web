package repositories

import org.springframework.data.neo4j.repository.GraphRepository
import models.content._
import org.springframework.data.neo4j.annotation.Query
import java.util.UUID

trait ContentPageRepository extends GraphRepository[ContentPage] {

  // Auto-mapped by Spring
  def findByobjectId(objectId: UUID): ContentPage
  def findByRouteAndContentState(route: String, contentState: String): ContentPage
  def findByRouteAndContentCategoriesAndVisibleInMenusAndContentState(route: String, contentCategories: Array[String], visibleInMenus: Boolean, contentState: String): ContentPage
  def findByContentCategoriesAndVisibleInMenusAndContentState(contentCategories: Array[String], visibleInMenus: Boolean, contentState: String): java.util.List[ContentPage]
  def findByContentCategories(contentCategories: Array[String]): java.util.List[ContentPage]

//  @Query("MATCH (parPage)-[:`PARENT_PAGE`]->(childPage) WHERE parPage.objectId={0} OR childPage.objectId={0} RETURN cntPage")
//  def findByParentPageObjectIdOrIsParentForObjectId(parentPageObjectId: UUID, isParentForObjectId: UUID): java.util.List[ContentPage]

}