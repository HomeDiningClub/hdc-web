package models.viewmodels
import java.util.UUID

import org.joda.time.DateTime

case class BloggPostItem (
                       objectId: Option[UUID],
                       title: String,
                       text : String,
                       mainImage: Option[String],
                       hasNext: Boolean,
                       hasPrevious: Boolean,
                       totalPages: Int,  
                       dateCreated : DateTime,
                       dateChanged : DateTime,
                       bloggPostObjectId : UUID
                       )

