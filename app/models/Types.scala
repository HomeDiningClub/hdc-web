package models

case class Type (
                    id: Long,
                    name: String,
                    key: String,
                    v: String)

object Types {

  var typeList = Set(
    Type(1L, "Amerikanskt", "Amerikanskt", "quality[0]"),
    Type(2L, "Italienskt", "Italienskt", "quality[1]"),
    Type(3L, "Franskt", "Franskt", "quality[2]"),
    Type(4L, "Asiatiskt", "Asiatiskt", "quality[3]"),
    Type(5L, "Svensk husman", "Svensk husman", "quality[4]"),
    Type(6L, "Mellanöstern", "Mellanöstern", "quality[5]"),
    Type(7L, "Vegetarisk", "Vegetarisk", "quality[6]"),
    Type(8L, "RAW-food", "RAW-food", "quality[7]"),
    Type(9L, "LCHF", "LCHF", "quality[8]"),
    Type(10L, "Koscher", "Koscher", "quality[9]"),
    Type(11L, "Vilt", "Vilt", "quality[10]"),
    Type(12L, "Kött", "Kött", "quality[11]"),
    Type(13L, "Fisk och skaldjur", "Fisk och skaldjur", "quality[12]"),
    Type(14L, "Lyx", "Lyx", "quality[13]"),
    Type(15L, "Budget", "Budget", "quality[14]"),
    Type(16L, "Barnvänligt", "Barnvänligt", "quality[15]"),
    Type(18L, "Friluftsmat", "Friluftsmat", "quality[16]"),
    Type(19L, "Drycker", "Drycker", "quality[17]"),
    Type(20L, "Efterrätter", "Efterrätter", "quality[18]"),
    Type(21L, "Bakverk", "Bakverk", "quality[19]")
  )
}

class Types {

      var vald : Set[String] = new collection.immutable.HashSet

  def addVald(key: String) {
        vald += key
  }

  def getString(key: String) : String =  {

  var markerad : String = ""


    if(vald.contains(key)) {
        markerad = "checked"
   }

   markerad
  }

  def findAll = Types.typeList.toList.sortBy(_.name)




  def add(element: Type){
    Types.typeList = Types.typeList + element
  }
}
