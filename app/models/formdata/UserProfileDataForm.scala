package models.formdata

case class UserProfileDataForm(
                                name: String,
                                name2: String,
                                aboutmeheadline: String,
                                aboutme: String,
                                county: String,
                                streetAddress: String,
                                zipCode: String,
                                city: String,
                                phoneNumber: String,
                                personnummer: String,
                                acceptTerms: Boolean,
                                mainimage: Option[String],
                                avatarimage: Option[String],
                                firstName: String,
                                lastName: String,
                                emailAddress: String,
                                emailAddress2: String
                                )
