package checkers

import BuiltChecker.given
import checkers.macros.IntrospectorMacros

inline given list[T, L <: List[T]](using inline I: Introspector[T]): Introspector[L] = {
  l =>
    var i = -1
    val doCheck = (t: T) => I.checkSelf(t)
    val checkedElements = l.foldLeft[Checked[L]](Right(l)) { (acc, current) =>
      i += 1
      Checker.combine(acc, doCheck(current).reportedAs("" + i))
    }
    Checker.combine(checkedElements, Introspector.basic[L].checkSelf(l))
}

inline given product[T <: Product]: Introspector[T] =
  IntrospectorMacros.makeForProduct[T]

final case class Person(
  name: String & AlphaNumeric,
  age: Int,
  things: List[String & SizeOf[3]]
)

final case class Perk(
  label: String,
  level: Int
)

type StandardizedPerk = {
  val label: String & AlphaNumeric & SizeOf[3] & Uppercase
  val level: Int & Positive
}

@main def testAll() = {
  val str = "ABC+"
  println(str.checkAs[AlphaNumeric & Uppercase])

  val strCasted = str.asInstanceOf[String & AlphaNumeric & Uppercase]
  println(List(strCasted).selfChecked)

  val person = Person(
    "James-".asInstanceOf,
    12,
    List("STREN".asInstanceOf[String & SizeOf[3]])
  )
  println(person.selfChecked)

  val perk = Perk("STR+", 12)
  println(perk.asInstanceOf[Perk & StandardizedPerk].selfChecked)
}