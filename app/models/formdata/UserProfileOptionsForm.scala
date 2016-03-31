package models.formdata

case class UserProfileOptionsForm(
                                   payCash: Boolean,
                                   paySwish: Boolean,
                                   payBankCard: Boolean,
                                   payIZettle: Boolean,
                                   wantsToBeHost: Boolean,
                                   tagList: Option[List[TagCheckboxForm]]
                                   )


