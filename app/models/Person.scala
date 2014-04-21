package models

case class Person (
id: Long,
fornamn: String,
efternamn: String)

object Person {

  var personLista = Set(
    Person(1L, "Sven", "Svensson"),
    Person(2L, "Olle", "Larsson"),
    Person(3L, "Olle", "Nyman"),
    Person(4L, "Dan", "Ytterberg"),
    Person(5L, "Viola", "Väst")
  )

  def findAll = personLista.toList.sortBy(_.fornamn)

  def findAllSortOnFirstName = findAll

  def findAllSortOnLastName = personLista.toList.sortBy(_.efternamn)

  def add(person: Person){
    personLista = personLista + person
  }

}
