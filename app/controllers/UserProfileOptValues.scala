package controllers

class UserProfileOptValues(
  var payCache      : String,
  var paySwish      : String,
  var payBankCard   : String,
  var payIZettle    : String,
  var numberOfGuest : String)
{

    def maxGuestsSelected(value : String) : String = {

      var defSelected = "selected"
      var isThisSelected = ""

      if(value == numberOfGuest ){
        isThisSelected = defSelected
      }

      isThisSelected
    }


    def isVald(value : String) : String = {

      var defSelected    : String = "CHECKED"
      var selectedString : String = ""

      if(value.size > 0) {
        selectedString = defSelected
      }
      selectedString
    }

    def ispayCache : String = {
      isVald(payCache)
    }

  def ispaySwish : String = {
    isVald(paySwish)
  }

  def ispayBankCard : String = {
    isVald(payBankCard)
  }

  def ispayIZettle : String = {
    isVald(payIZettle)
  }

  def getnumberOfGuest : String = {
    numberOfGuest
  }


}
