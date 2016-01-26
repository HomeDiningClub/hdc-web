package controllers

class UserProfileOptValues(
                            var payCache: String,
                            var paySwish: String,
                            var payBankCard: String,
                            var payIZettle: String,
                            var roleGuest: String,
                            var roleHost: String,
                            var numberOfGuest: String,
                            var minGuest: String,
                            var handicapFriendly: String,
                            var childFfriendly: String,
                            var havePets: String,
                            var smoke: String
                            ) {

  def maxGuestsSelected(value: String): String = {

    var defSelected = "selected"
    var isThisSelected = ""

    if (value == numberOfGuest) {
      isThisSelected = defSelected
    }

    isThisSelected
  }


  def minGuestsSelected(value: String): String = {

    var defSelected = "selected"
    var isThisSelected = ""

    if (value == minGuest) {
      isThisSelected = defSelected
    }

    isThisSelected
  }


  /**
   * Check if value is equals to value of checkedValue when
   * make the value checked
   * @param value
   * @param checkedValue
   * @return
   */
  def isValueToCheck(value: String, checkedValue: String): String = {

    var defSelected: String = "CHECKED"
    var selectedString: String = ""

    if (value.equalsIgnoreCase(checkedValue)) {
      selectedString = defSelected
    }

    selectedString
  }


  def smokingPermittedJa(): String = {
    isValueToCheck(smoke, "Ja")
  }

  def smokingPermittedNej(): String = {
    isValueToCheck(smoke, "Nej")
  }


  def havePetsJa(): String = {
    isValueToCheck(havePets, "Ja")
  }

  def havePetsNej(): String = {
    isValueToCheck(havePets, "Nej")
  }


  def childFfriendlyJa(): String = {
    isValueToCheck(childFfriendly, "Ja")
  }

  def childFfriendlyNej(): String = {
    isValueToCheck(childFfriendly, "Nej")
  }


  def handicapFriendlyJa(): String = {
    isValueToCheck(handicapFriendly, "Ja")
  }

  def handicapFriendlyNej(): String = {
    isValueToCheck(handicapFriendly, "Nej")
  }


  def isVald(value: String): String = {

    var defSelected: String = "CHECKED"
    var selectedString: String = ""

    if (value.size > 0) {
      selectedString = defSelected
    }
    selectedString
  }

  def isBooleanSelected(value: String): Boolean = {

    var isSelected: Boolean = false

    if (value.size > 0) {
      isSelected = true
    }
    isSelected
  }


  def ispayCache: String = {
    isVald(payCache)
  }

  def ispaySwish: String = {
    isVald(paySwish)
  }

  def ispayBankCard: String = {
    isVald(payBankCard)
  }

  def ispayIZettle: String = {
    isVald(payIZettle)
  }

  def isroleGuest: String = {
    isVald(roleGuest)
  }

  def isBooleanSelectedGuest: Boolean = {
    isBooleanSelected(roleGuest)
  }


  def isroleHost: String = {
    isVald(roleHost)
  }

  def isBooleanSelectedHost: Boolean = {
    isBooleanSelected(roleHost)
  }

  def getnumberOfGuest: String = {
    numberOfGuest
  }

  def getmMinNumberOfGuest: String = {
    minGuest
  }


}
