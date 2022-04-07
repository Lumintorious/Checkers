package checkers

import cats.data.Validated

export number.{given, *}

object number {
  opaque type GT[X] = Any
  inline given gt[N, T <: N](using NM: Numeric[N]): Checker[N, GT[T]] =
    Checker[N, GT[T]](n => NM.gt(n, valueOf[T]))

  opaque type LT[X] = Any
  inline given lt[N, T <: N](using NM: Numeric[N]): Checker[N, LT[T]] =
    Checker[N, LT[T]] { NM.lt(_, valueOf[T]) }

  type GTEQ[X] = GT[X] | SameAs[X]
  type LTEQ[X] = LT[X] | SameAs[X]
  type Between[L, H] = GT[H] & LT[L]
  type IsZero = SameAs[0]
  type NonZero = Not[IsZero]

  type Positive = GT[0]
  type Negative = LT[0]
  type PositiveOrZero = GTEQ[0]
  type NegativeOrZero = LTEQ[0]

  opaque type Integral = Any
  inline given integral[N](using NM: Numeric[N]): Checker[N, Integral] =
    Checker[N, Integral] { NM.toDouble(_) % 1 == 0 }

  type Decimal = Not[Integral]

  opaque type Finite = Any
  inline given finite[N](using NM: Numeric[N]): Checker[N, Finite] =
    Checker[N, Finite] { NM.toDouble(_).isFinite }

  type Infinite = Not[Finite]

  opaque type Even = Any
  inline given even[N](using NM: Numeric[N]): Checker[N, Even] =
    Checker[N, Even] { NM.toDouble(_) % 2 == 0 }

  type Odd = Integral & Not[Even]

  // opaque type PerfectSquare = Any
  // inline given perfectSquare[N](using NM: Numeric[N]): Checker[N, PerfectSquare] =
    // n => Validated.condNel(Math.sqrt(NM.toDouble(n)) % 1 == 0, n, s"'${n}' was not a perfect square")
}
