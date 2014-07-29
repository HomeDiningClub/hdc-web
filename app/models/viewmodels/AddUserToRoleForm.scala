package models.viewmodels

case class AddUserToRoleForm(
                       userObjectId: String,
                       roleObjectId: String,
                       addOrRemoveRole: Boolean = true
                       )
{ }
