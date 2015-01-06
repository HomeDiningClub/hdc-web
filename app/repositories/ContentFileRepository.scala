package repositories

import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.repository.GraphRepository
import models.files._
import java.util
import java.util.UUID


trait ContentFileRepository extends GraphRepository[ContentFile] {

  @Query("MATCH (n:`ContentFile`) WHERE n.objectId={0} RETURN n")
  def findByobjectId(objectId: UUID): ContentFile

  @Query("MATCH (n:`ContentFile`) WHERE n.objectId IN {0} RETURN n")
  def findByListOfobjectIds(listOfobjectIds: Array[String]): util.List[ContentFile]

  @Query("MATCH (n:`ContentFile`)-[:`OWNER`]-(u:`UserCredential`) WHERE u.objectId={0} AND n.baseContentType={1} RETURN n ORDER BY n.createdDate DESC")
  def findByownerObjectIdAndContentType(objectId: String, contentType: String): util.List[ContentFile]

  @Query("MATCH (n:`ContentFile`)-[:`OWNER`]-(u:`UserCredential`) WHERE n.objectId={0} AND u.objectId={1} RETURN n")
  def findByobjectIdAndownerObjectId(objectId: String, userObjectId: String): ContentFile

  @Query(
    "MATCH (n:`ContentFile`)" +
    " WHERE id(n)" +
    " OPTIONAL MATCH (n)-[r]-()" +
    " DELETE r,n"
  )
  def deleteAllRelationsAndDelete(file: ContentFile)

  // Auto-mapped by Spring
  def findBybaseContentType(name: String): util.List[ContentFile]

}
