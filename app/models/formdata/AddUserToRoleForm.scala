package models.formdata

case class AddUserToRoleForm(
                       userObjectId: String,
                       roleObjectId: String,
                       addOrRemoveRole: Boolean = true
                       )
{ }
