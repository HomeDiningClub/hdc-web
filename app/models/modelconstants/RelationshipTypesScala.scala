package models.modelconstants

import org.neo4j.graphdb.RelationshipType

object RelationshipTypesScala {

  object REACHABLE_BY_ROCKET extends RelationshipType {
   final val Constant = "REACHABLE_BY_ROCKET"
   def name(): String = Constant
  }

  object  RESPONSE extends RelationshipType {
    final val Constant = "RESPONSE"
    def name(): String = Constant
  }

  object  INCOMING_MESSAGE {
    final val Constant = "INCOMING_MESSAGE"
    def name(): String = Constant
  }

  object  OUTGOING_MESSAGE {
    final val Constant = "OUTGOING_MESSAGE"
    def name(): String = Constant
  }

  object  REQUEST {
    final val Constant = "REQUEST"
    def name(): String = Constant
  }

  object  REPLY {
    final val Constant = "REPLY"
    def name(): String = Constant
  }

  object AVATAR_IMAGE extends RelationshipType {
    final val Constant = "AVATAR_IMAGE"
    def name(): String = Constant
  }

  object LIKES_USER extends RelationshipType {
    final val Constant = "LIKES_USER"
    def name(): String = Constant
  }

  object LIKES_RECIPE extends RelationshipType {
    final val Constant = "LIKES_RECIPE"
    def name(): String = Constant
  }

  object MAIN_IMAGE extends RelationshipType  {
    final val Constant = "MAIN_IMAGE"
    def name(): String = Constant
  }

  object RELATED_PAGE extends RelationshipType {
    final val Constant = "RELATED_PAGE"
    def name(): String = Constant
  }

  object IN_ROLE extends RelationshipType {
    final val Constant = "IN_ROLE"
    def name(): String = Constant
  }

   object OWNER extends RelationshipType {
     final val Constant = "OWNER"
     def name(): String = Constant
   }

  object IMAGES extends RelationshipType {
    final val Constant = "IMAGES"
    def name(): String = Constant
  }

  object RATED_USER extends RelationshipType {
    final val Constant = "RATED_USER"
    def name(): String = Constant
  }

  object RATED_RECIPE extends RelationshipType {
    final val Constant = "RATED_RECIPE"
    def name(): String = Constant
  }

  object HAS_RECIPES extends RelationshipType {
    final val Constant = "HAS_RECIPES"
    def name(): String = Constant
  }

  object RECOMMENDED extends RelationshipType {
    final val Constant = "RECOMMENDED"
    def name(): String = Constant
  }

  object ALCOHOL_SERVING extends RelationshipType {
    final val Constant = "ALCOHOL_SERVING"
    def name(): String = Constant
  }

   object FILE_TRANSFORMATION extends RelationshipType {
     final val Constant = "FILE_TRANSFORMATION"
     def name(): String = Constant
   }

   object CONTENT_STATE extends RelationshipType {
     final val Constant = "CONTENT_STATE"
     def name(): String = Constant
   }

   object PROFILE_CREDENTIAL extends RelationshipType {
     final val Constant = "PROFILE_CREDENTIAL"
     def name(): String = Constant
   }

   object PROFILE_LOCATION extends RelationshipType {
     final val Constant = "PROFILE_LOCATION"
     def name(): String = Constant
   }

  object BOOKED_EVENT_DATE extends RelationshipType {
    final val Constant = "BOOKED_EVENT_DATE"
    def name(): String = Constant
  }

 }
