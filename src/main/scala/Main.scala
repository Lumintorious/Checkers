import confinement.{given, *}
import confinement.unsafe.*
import confinement.macros.*
import cats.syntax.validated.*

import eng.given

trait Entity {
  def age: Int
}

object Entity {
  opaque type CanVote = Any
  inline given Checker[Entity, CanVote] =
    Checker { _.age >= 18 }
}

case class Person(name: String, age: Int) extends Entity
case class Hamster(name: String, age: Int) extends Entity

type Cute = Hamster

@main def hello: Unit =
  
  val razvan: Entity = Person("Razvan", 24)
  val zapacel: Entity = Hamster("Zapacel", 1)
  val razvanResult = razvan.validateAs[Person & Entity.CanVote]
  val zapacelResult = zapacel.validateAs[Person & Entity.CanVote]
  println(razvanResult)
  println(zapacelResult)

  
