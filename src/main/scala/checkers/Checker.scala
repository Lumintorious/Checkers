package checkers

import scala.util.NotGiven
import compiletime.*
import scala.annotation.implicitNotFound
import cats.data.NonEmptyList
import scala.annotation.StaticAnnotation
import checkers.macros.TypeShow
import checkers.macros.CheckerMacros

type Checked[T] = Either[List[FailedCheck], T]

opaque type BuiltChecker[A, B] <: Checker[A, B] = Checker[A, B]

object BuiltChecker {
  transparent inline given synthesize[T, Types](using NotGiven[Checker[T, Types]]): BuiltChecker[T, Types] =
    CheckerMacros.make[T, Types].built
}

extension [A, B] (checker: Checker[A, B]) def built: BuiltChecker[A, B] = checker

trait Checker[Actual, Potential] private[checkers] {
  self =>
  def confine(actual: Actual): Checked[Actual & Potential]

  inline def && [NewPotential](other: Checker[Actual, NewPotential]): BuiltChecker[Actual, Potential & NewPotential] =
    Checker.Combined[Actual, Potential, NewPotential](this, other).built

  inline def || [NewPotential](other: Checker[Actual, NewPotential]): BuiltChecker[Actual, Potential | NewPotential] =
    Checker.AnyOne[Actual, Potential, NewPotential](this, other).built

  inline def negated(using TS: TypeShow[Potential]): BuiltChecker[Actual, Not[Potential]] =
    Checker.Negated[Actual, Potential](this).built

  inline def ifBroken(formatter: Actual => String): BuiltChecker[Actual, Potential] =
    Checker.NamedIfBroken[Actual, Potential](this, formatter).built

  inline def map(fn: Checked[Actual & Potential] => Checked[Actual & Potential]): Checker[Actual, Potential] =
    Checker.Mapped(this, fn)
}

object Checker {
  inline given identity[T]: Checker[T, T] =
    Checker[T, T] { _ => true }

  inline def combine[Actual](v1: Checked[Actual], v2: Checked[?]): Checked[Actual] = {
    (v1, v2) match {
    case (va @ Right(a), Right(b)) => va 
    case (va @ Left(a), Right(b)) => va 
    case (Right(a), vb @ Left(b)) => Left(b)
    case (Left(a), Left(b)) => Left(a ::: b)
    }
  }

  class Combined[Actual, Potential, NewPotential](
    self: Checker[Actual, Potential],
    other: Checker[Actual, NewPotential],
  ) extends Checker[Actual, Potential & NewPotential] {
    inline def confine(actual: Actual): Checked[Actual & Potential & NewPotential] =
      val first: Checked[Actual & Potential] = self.confine(actual)
      val second: Checked[Actual & NewPotential] = other.confine(actual)
      combine[Actual](first, second)
      .asInstanceOf[Checked[Actual & Potential & NewPotential]]
    }
  
  class AnyOne[Actual, Potential, NewPotential](
    self: Checker[Actual, Potential],
    other: Checker[Actual, NewPotential],
  ) extends Checker[Actual, Potential | NewPotential] {
    inline def combine(v1: Checked[Actual & Potential], v2: Checked[Actual & NewPotential]): Checked[Actual & (Potential | NewPotential)] = {
      (v1, v2) match {
      case (va @ Right(a), Right(b)) => va 
      case (Left(a), va @ Right(b)) => Right(b)
      case (va @ Right(a), Left(b)) => va
      case (Left(a), Left(b)) => Left(a ::: b)
      }
    }
    inline def confine(actual: Actual): Checked[Actual & (Potential | NewPotential)] = 
        // combine(self.confine(actual), other.confine(actual))
        // .asInstanceOf[Checked[Actual & (Potential & NewPotential)]]
      ???
      }

  class Negated[Actual, Potential](
    inner: Checker[Actual, Potential]
  )(using TS: TypeShow[Potential]) extends Checker[Actual, Not[Potential]] {
    inline def confine(actual: Actual): Checked[Actual & Not[Potential]] = 
      inner.confine(actual).fold(
        list => Right(actual.asInstanceOf[Actual & Not[Potential]]),
        a => Left(List(FailedCheck.explain(s"${a} was ${TS.show}")))
      )
  }

  class NamedIfBroken[Actual, Potential](
    underlying: Checker[Actual, Potential],
    ifBroken: Actual => String
  ) extends Checker[Actual, Potential] {
    def confine(actual: Actual): Checked[Actual & Potential] = {
      underlying
        .confine(actual)
        .swap
        .map(l => l.map( elem =>
          if(elem.isBasic) {
            elem.copy(
              explanation = ifBroken.apply(actual),
              isBasic = false
            )
          } else {
            elem
          }
        ))
        .swap
    }
  }

  class Mapped[Actual, Potential](
    underlying: Checker[Actual, Potential],
    fn: Checked[Actual & Potential] => Checked[Actual & Potential],
  ) extends Checker[Actual, Potential] {
    def confine(actual: Actual): Checked[Actual & Potential] = {
      fn(underlying.confine(actual))
    }
  }

  inline def apply[From, To](
    check: From => Boolean
  )(using ev: From =:= (From & To)) = {
    new Checker[From, To] {
      def confine(actual: From): Checked[From & To] = {
        Either.cond(check(actual), ev(actual), List(FailedCheck.explain(
          s"'${actual}' is not valid"
        )))
      }
    }
  }
}