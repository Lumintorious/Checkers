package checkers

import compiletime.*

export number.{given, *}
export number.guarantees.{given, *}

private inline def v[T] = valueOf[T]

object number {
  opaque type Greater[X] = Any
  inline given gt[N, N2, T <: N2](using NM: Numeric[N], NM2: Numeric[N2]): Checker[N, Greater[T]] =
    Checker { NM.toDouble(_) > NM2.toDouble(v[T]) }

  opaque type Less[X] = Any
  inline given lt[N, N2, T <: N2](using NM: Numeric[N], NM2: Numeric[N2]): Checker[N, Less[T]] =
    Checker { NM.toDouble(_) < NM2.toDouble(v[T]) }

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

  type Integral = DivisibleBy[1]
  type Decimal = Not[Integral]

  opaque type Finite = Any
  inline given finite[N](using NM: Numeric[N]): Checker[N, Finite] =
    Checker[N, Finite] { NM.toDouble(_).isFinite }

  type Infinite = Not[Finite]

  type Even = DivisibleBy[2]
  type Odd = Integral & Not[Even]

  opaque type DivisibleBy[N] = Any
  inline given divisibleBy[N, N2, T <: N2](using NM: Numeric[N], NM2: Numeric[N2]): Checker[N, DivisibleBy[T]] =
    Checker[N, Even] { NM.toDouble(_) % NM2.toDouble(v[T]) == 0 }

  object guarantees {
    // GREATER
    inline given gtGInt[Tpe <: Int & Singleton, N <: Tpe, Thr <: Tpe]: Guarantee[N, Greater[Thr]] =
      Guarantee(v[N] > v[Thr])
      
    inline given gtGLong[Tpe <: Long & Singleton, N <: Tpe, Thr <: Tpe]: Guarantee[N, Greater[Thr]] =
      Guarantee(v[N] > v[Thr])

    inline given gtGFloat[Tpe <: Float & Singleton, N <: Tpe, Thr <: Tpe]: Guarantee[N, Greater[Thr]] =
      Guarantee(v[N] > v[Thr])

    inline given gtGDouble[Tpe <: Double & Singleton, N <: Tpe, Thr <: Tpe]: Guarantee[N, Greater[Thr]] =
      Guarantee(v[N] > v[Thr])

    // LESS
    inline given ltGInt[N <: Int & Singleton, Thr <: Int]: Guarantee[N, Less[Thr]] =
      Guarantee(v[N] < v[Thr])
      
    inline given ltGLong[N <: Long & Singleton, Thr <: Long]: Guarantee[N, Less[Thr]] =
      Guarantee(v[N] < v[Thr])

    inline given ltGFloat[N <: Float & Singleton, Thr <: Float]: Guarantee[N, Less[Thr]] =
      Guarantee(v[N] < v[Thr])

    inline given ltGDouble[N <: Double & Singleton, Thr <: Double]: Guarantee[N, Less[Thr]] =
      Guarantee(v[N] < v[Thr])
    
    // INTEGRAL
    inline given integralGInt[N <: Int & Singleton]: Guarantee[N, Integral] =
      Guarantee()

    inline given integralGLong[N <: Long & Singleton]: Guarantee[N, Integral] =
      Guarantee()

    inline given integralGFloat[N <: Float & Singleton]: Guarantee[N, Integral] =
      Guarantee(v[N] % 1 == 0)

    inline given integralGDouble[N <: Double & Singleton]: Guarantee[N, Integral] =
      Guarantee(v[N] % 1 == 0)

    // DIVISIBLEBY
    inline given divGInt[N <: Int & Singleton, Thr <: Int]: Guarantee[N, DivisibleBy[Thr]] =
      Guarantee(v[N] % v[Thr] == 0)

    inline given divGLong[N <: Long & Singleton, Thr <: Long]: Guarantee[N, DivisibleBy[Thr]] =
      Guarantee(v[N] % v[Thr] == 0)

    inline given divGFloat[N <: Float & Singleton, Thr <: Float]: Guarantee[N, DivisibleBy[Thr]] =
      Guarantee(v[N] % v[Thr] == 0)

    inline given divGDouble[N <: Double & Singleton, Thr <: Double]: Guarantee[N, DivisibleBy[Thr]] =
      Guarantee(v[N] % v[Thr] == 0)
  }
}
