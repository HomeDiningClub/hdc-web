package models

import java.util
import org.springframework.data.neo4j.annotation._

@QueryResult
  trait BlogPostsData {

    @ResultColumn("b.title")
    def getTitle() : String

    @ResultColumn("b.text")
    def getText() : String

    @ResultColumn("b.objectId")
    def getBlogPostObjectId() : String

    @ResultColumn("b.lastModifiedDate")
    def getLastModDate() : String

    @ResultColumn("b.contentState")
    def getState() : String

    @ResultColumn("b.createdDate")
    def getDateCreated() : String

    @ResultColumn("MainImage")
    def getMainImage() : util.List[String]

}