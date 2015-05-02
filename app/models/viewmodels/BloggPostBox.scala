package models.viewmodels

//case class BloggPostBox()

import java.util.UUID

import org.joda.time.DateTime

// Used on the Start page, collects profile information and user information
case class BloggPostBox (
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

