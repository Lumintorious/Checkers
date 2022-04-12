package checkers

export number.{given, *}

object number {
  opaque type Greater[X] = Any
  inline given gt[N, N2, T <: N2](using NM: Numeric[N], NM2: Numeric[N2]): Checker[N, Greater[T]] =
    Checker { NM.toDouble(_) > NM2.toDouble(valueOf[T]) }

  opaque type Less[X] = Any
  inline given lt[N, N2, T <: N2](using NM: Numeric[N], NM2: Numeric[N2]): Checker[N, Less[T]] =
    Checker { NM.toDouble(_) < NM2.toDouble(valueOf[T]) }

  type GT[X] = Greater[X]
  type LT[X] = Less[X]
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
}
