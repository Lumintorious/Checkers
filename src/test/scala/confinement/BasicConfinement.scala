package confinement

class MySuite extends munit.FunSuite {
  test("Basic confinements should be validated correctly") {
    val (a, b, c) = (0, 10, -10)

    assert { a.validateAs[Positive].isInvalid }
    assert { b.validateAs[Positive].isValid   }
    assert { c.validateAs[Positive].isInvalid }
    assert { c.validateAs[Positive].fold(_.head.explanation == "'-10' is not Positive", _ => false) }
  }

  test("Intersection confinements should be validated correctly") {
    val (n, p) = (-10, 10)

    assert { n.validateAs[Positive & NonZero].isInvalid }
    assert { n.validateAs[Positive & NonZero] == n.validateAs[Positive] }

  }
}
