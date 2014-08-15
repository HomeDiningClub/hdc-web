package models.modelconstants

import org.neo4j.graphdb.RelationshipType

object RelationshipTypesScala {

   object REACHABLE_BY_ROCKET extends RelationshipType {
     final val Constant = "REACHABLE_BY_ROCKET"
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

  object USED_BY {
    final val Constant = "USED_BY"
    def name(): String = Constant
  }

  object RATED {
    final val Constant = "RATED"
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
