// import checkers.{given, *}
// import checkers.unsafe.*
// import checkers.macros.*
// import cats.syntax.validated.*
// import io.circe.parser.decode
// import io.circe.*
// import io.circe.parser.*
// import scala.util.NotGiven
// import scala.deriving.Mirror
// import scala.annotation.compileTimeOnly

// import checkers.eng.given

// inline given list[T, L <: List[T]](using inline SC: SelfChecker[T]): SelfChecker[L] = {
//   l =>
//     var i = -1
//     val doCheck = (t: T) => SC.check(t)
//     val checkedElements = l.foldLeft[Checked[L]](Right(l)) { (acc, current) =>
//       i += 1
//       Checker.combine(acc, doCheck(current).reportedAs("" + i))
//     }
//     Checker.combine(checkedElements, SelfChecker.primitive[L].check(l))
// }

// inline given caseClass[C <: Product]: SelfChecker[C] =
//   ???//SelfCheckMacros2.makeForCaseClass[C]

// inline given decoderOfAll[T]: Decoder[T] = CheckedDecoderMacros2.makeBasic[T]
// inline given encoderOfAll[T]: Encoder[T] = ???

// final case class Person(
//   name: String & AlphaNumeric,
//   age: Int & Positive,
//   perks: List[Perk & StandardizedPerk]
// ) derives Codec.AsObject

// final case class Perk(
//   name: String,
//   level: Int & Positive
// ) derives Codec.AsObject

// type StandardizedPerk = {
//   val name: String & SizeOf[3] & Uppercase
//   val level: Int & Positive & Less[100]
// }

// enum State {
//   case Ongoing
//   case Won
//   case Lost
// }

// case class Game (
//   round: Int,
//   state: State
// )

// @main def hello: Unit =
//   val strAndStuff = "123+".asInstanceOf[String & AlphaNumeric & SizeOf[2]]
//   val lst = List(strAndStuff, "gg", "wot+").asInstanceOf[List[String & AlphaNumeric & SizeOf[2]] & SizeOf[2]]
//   // println(strAndStuff.selfChecked)
//   println(lst.selfChecked)
//   // LIST WORKS

//   // val strAndStuff = "123+".asInstanceOf[String & AlphaNumeric & SizeOf[2]]
//   val perk = Perk.apply.checking("INT", -67)
//   println(perk)
//   // val person = Person(
//   //   "Razvan+".asInstanceOf,
//   //   2.asInstanceOf,
//   //   List("POW", "POW", "STR", "MANA").asInstanceOf
//   // )

//   // val game = Game(1, State.Ongoing)
//   // println(game.checkAs[{ val state: State.Lost.type }])

//   val personJson = """
//     {
//       "name": "Jason",
//       "age": 14,
//       "perks": [
//         {
//           "name": "STRENGTH",
//           "level": 24
//         }
//       ]
//     }
//   """

//   val strJson = """ "something--" """

//   case class Thing(value: String, number: Int)

//   val jason = decode[Person](personJson)
//   val str = decode[String & AlphaNumeric](strJson)
//   val thing = Thing("Clock", -12)
//   println(str)
//   println(jason)
//   type X = {
//     val value: String & SizeOf[2]
//     val number: Int & Positive
//   }
//   val things = List(thing)
//   val t = things.asInstanceOf[List[Thing & X]].selfChecked
//   println(things.asInstanceOf[List[Thing & X]].selfChecked)
  
