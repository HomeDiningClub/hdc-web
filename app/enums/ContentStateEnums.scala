package enums

object ContentStateEnums extends Enumeration {
  type ContentStateEnums = Value
  val PUBLISHED, UNPUBLISHED, BANNED, FLAGGED = Value
}
