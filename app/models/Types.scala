package models

case class Type (
                    id: Long,
                    name: String,
                    key: String)

object Types {

  //

  var typeList = Set(
    Type(1L, "Amerikanskt", "Amerikanskt"),
    Type(2L, "Italienskt", "Italienskt"),
    Type(3L, "Franskt", "Franskt"),
    Type(4L, "Asiatiskt", "Asiatiskt"),
    Type(5L, "Svensk husman", "Svensk husman"),
    Type(6L, "Mellanöstern", "Mellanöstern"),
    Type(7L, "Vegetarisk", "Vegetarisk"),
    Type(8L, "RAW-food", "RAW-food"),
    Type(9L, "LCHF", "LCHF"),
    Type(10L, "Koscher", "Koscher"),
    Type(11L, "Vilt", "Vilt"),
    Type(12L, "Kött", "Kött"),
    Type(13L, "Fisk och skaldjur", "Fisk och skaldjur"),
    Type(14L, "Lyx", "Lyx"),
    Type(15L, "Budget", "Budget"),
    Type(16L, "Barnvänligt", "Barnvänligt"),
    Type(18L, "Friluftsmat", "Friluftsmat"),
    Type(19L, "Drycker", "Drycker"),
    Type(20L, "Efterrätter", "Efterrätter"),
    Type(21L, "Bakverk", "Bakverk")
  )
}

class Types {
  def findAll = Types.typeList.toList.sortBy(_.name)

  def add(element: Type){
    Types.typeList = Types.typeList + element
  }
}
