// package checkers

// import BuiltChecker.given
// import checkers.macros.IntrospectorMacros
// import io.circe.Codec
// import io.circe.parser.*
// import io.circe.syntax.*

// import scala.deriving.Mirror
// import checkers.macros.TypeShow
// import IntrospectedCodec.{*, given}
// import checkers.macros.HasConcrete

// inline given product[T <: Product]: Introspector[T] =
//   IntrospectorMacros.makeForProduct[T]

// inline given list[T, L <: List[T]](using inline I: Introspector[T]): Introspector[L] = {
//   l =>
//     var i = -1
//     val doCheck = (t: T) => I.checkSelf(t)
//     val checkedElements = l.foldLeft[Checked[L]](Right(l)) { (acc, current) =>
//       i += 1
//       Checker.combine(acc, doCheck(current).reportedAs("" + i))
//     }
//     Checker.combine(checkedElements, Introspector.checkLevel(l))
// }
// enum Gender derives Codec.AsObject {
//   case Male
//   case Female
// } 

// final case class Person(
//   name: String & AlphaNumeric,
//   age: Int & Positive,
//   perks: List[Perk & StandardizedPerk]
// ) derives Codec.AsObject, Introspector

// final case class Perk(
//   label: String,
//   level: Int
// ) derives Codec.AsObject

// type StandardizedPerk = {
//   val label: String & AlphaNumeric & SizeOf[3] & Uppercase
//   val level: Int & Positive & Less[100]
// }

// @main def testAll() = {
//   val str = "ABC"
//   val strR = str.checkAs[AlphaNumeric & Uppercase]
//   println(strR)

//   val strCasted = str.asInstanceOf[String & AlphaNumeric & Uppercase]
//   println(List(strCasted).selfChecked)

//   val person = Person(
//     "James-".asInstanceOf,
//     12.asInstanceOf,
//     List(Perk("STR", 12).asInstanceOf[Perk & StandardizedPerk])
//   )
//   println(List(person).selfChecked)

//   val perk = Perk("STR+", 12)
//   println(perk.asInstanceOf[Perk & StandardizedPerk].selfChecked)

//   val json = """
//     {
//       "name": "Jackie+",
//       "age": -67,
//       "perks": [
//         {
//           "label": "STR",
//           "level": -45
//         },
//         {
//           "label": "DEX",
//           "level": 23
//         },
//         {
//           "label": "INT",
//           "level": 4
//         }
//       ]
//     }
//   """

//   val introspected = IntrospectedCodec.decodeAndIntrospect[List[Person]](jsonTest)
//   println("Introspected:\n" + introspected)
// }