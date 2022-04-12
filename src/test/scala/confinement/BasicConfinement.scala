// package checkers

// class MySuite extends munit.FunSuite {
//   test("Basic confinements should be validated correctly") {
//     val (a, b, c) = (0, 10, -10)

//     assert { a.checkAs[Positive].isInvalid }
//     assert { b.checkAs[Positive].isValid   }
//     assert { c.checkAs[Positive].isInvalid }
//     assert { c.checkAs[Positive].fold(_.head.explanation == "'-10' is not Positive", _ => false) }
//   }

//   test("Intersection confinements should be validated correctly") {
//     val (n, p) = (-10, 10)

//     assert { n.checkAs[Positive & NonZero].isInvalid }
//     assert { n.checkAs[Positive & NonZero] == n.checkAs[Positive] }

//   }
// }
