package interfaces

import java.util.UUID

trait IEditable {
  def isEditableBy(objectId: UUID): java.lang.Boolean
}
