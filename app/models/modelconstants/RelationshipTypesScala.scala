package models.modelconstants

import org.neo4j.graphdb.RelationshipType

object RelationshipTypesScala {

   object REACHABLE_BY_ROCKET extends RelationshipType {
     final val Constant = "REACHABLE_BY_ROCKET"
     def name(): String = Constant
   }

  object  MESSAGE {
    final val Constant = "MESSAGE"
    def name(): String = Constant
  }

  object  RESPONSE {
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

  object AVATAR_IMAGE {
    final val Constant = "AVATAR_IMAGE"
    def name(): String = Constant
  }

  object LIKES_USER {
    final val Constant = "LIKES_USER"
    def name(): String = Constant
  }

  object LIKES_RECIPE {
    final val Constant = "LIKES_RECIPE"
  }

  object MAIN_IMAGE {
    final val Constant = "MAIN_IMAGE"
    def name(): String = Constant
  }

  object RELATED_PAGE {
    final val Constant = "RELATED_PAGE"
    def name(): String = Constant
  }

  object IN_ROLE {
    final val Constant = "IN_ROLE"
    def name(): String = Constant
  }

   object OWNER {
     final val Constant = "OWNER"
     def name(): String = Constant
   }

  object IMAGES {
    final val Constant = "IMAGES"
    def name(): String = Constant
  }

  object RATED_USER {
    final val Constant = "RATED_USER"
    def name(): String = Constant
  }

  object RATED_RECIPE {
    final val Constant = "RATED_RECIPE"
    def name(): String = Constant
  }

  object HAS_RECIPES {
    final val Constant = "HAS_RECIPES"
    def name(): String = Constant
  }

  object RECOMMENDED {
     final val Constant = "RECOMMENDED"
     def name(): String = Constant
   }

   object FILE_TRANSFORMATION {
     final val Constant = "FILE_TRANSFORMATION"
     def name(): String = Constant
   }

   object CONTENT_STATE {
     final val Constant = "CONTENT_STATE"
     def name(): String = Constant
   }

   object PROFILE_CREDENTIAL {
     final val Constant = "PROFILE_CREDENTIAL"
     def name(): String = Constant
   }

   object PROFILE_LOCATION {
     final val Constant = "PROFILE_LOCATION"
     def name(): String = Constant
   }
 }
