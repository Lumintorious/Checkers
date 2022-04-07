package checkers

import scala.util.NotGiven
import compiletime.*
import scala.annotation.implicitNotFound
import cats.data.{ValidatedNel, Validated}
import cats.data.Validated.Valid
import cats.data.Validated.Invalid
import checkers.macros.TypeShow
import cats.data.NonEmptyList

type ValidatedConfinement[T] = ValidatedNel[FailedCheck, T]

trait Checker[Actual, Potential] private[checkers] {
  self =>
  def confine(actual: Actual): ValidatedConfinement[Actual & Potential]

  inline def && [NewPotential](other: Checker[Actual, NewPotential]): Checker[Actual, Potential & NewPotential] =
    Checker.Combined[Actual, Potential, NewPotential](this, other)

  inline def || [NewPotential](other: Checker[Actual, NewPotential]): Checker[Actual, Potential | NewPotential] =
    Checker.Either[Actual, Potential, NewPotential](this, other)

  inline def negated: Checker[Actual, Not[Potential]] =
    Checker.Negated[Actual, Potential](this)

  inline def widen[BroaderPotential](using TS: TypeShow[BroaderPotential], ev: (Potential <:< BroaderPotential)): Checker[Actual, BroaderPotential] =
    new Checker[Actual, BroaderPotential] {
      def confine(actual: Actual) =
        self.confine(actual).leftMap(l => NonEmptyList.of(FailedCheck.explain(s"${actual} not ${TS.show}")))
        .map(_.asInstanceOf[Actual & BroaderPotential])
    }
}

object Checker {
  inline given identity[T]: Checker[T, T] =
    Checker[T, T] { _ => true }

  transparent inline given intersect[T, Types]: Checker[T, Types] =
    macros.ConfinementAccumulator.accumulate[T, Types]

  class Combined[Actual, Potential, NewPotential](
    self: Checker[Actual, Potential],
    other: Checker[Actual, NewPotential],
  ) extends Checker[Actual, Potential & NewPotential] {
    type V = ValidatedNel[checkers.FailedCheck, Actual]
    inline def combine(v1: V, v2: V): V = {
      (v1, v2) match {
      case (va @ Valid(a), Valid(b)) => va 
      case (va @ Invalid(a), Valid(b)) => va 
      case (Valid(a), vb @ Invalid(b)) => vb
      case (Invalid(a), Invalid(b)) => Invalid(a ::: b)
      }
    }
    inline def confine(actual: Actual): ValidatedNel[FailedCheck, Actual & Potential & NewPotential] = 
        combine(self.confine(actual), other.confine(actual))
        .asInstanceOf[ValidatedNel[FailedCheck, Actual & Potential & NewPotential]]
      }
  
  class Either[Actual, Potential, NewPotential](
    self: Checker[Actual, Potential],
    other: Checker[Actual, NewPotential],
  ) extends Checker[Actual, Potential | NewPotential] {
    type V = ValidatedNel[checkers.FailedCheck, Actual]
    inline def combine(v1: V, v2: V): V = {
      (v1, v2) match {
      case (va @ Valid(a), Valid(b)) => va 
      case (Invalid(a), va @ Valid(b)) => va 
      case (va @ Valid(a), Invalid(b)) => va
      case (Invalid(a), Invalid(b)) => Invalid(a ::: b)
      }
    }
    inline def confine(actual: Actual): ValidatedNel[FailedCheck, Actual & (Potential | NewPotential)] = 
        combine(self.confine(actual), other.confine(actual))
        .asInstanceOf[ValidatedNel[FailedCheck, Actual & (Potential & NewPotential)]]
      }

  class Negated[Actual, Potential](
    inner: Checker[Actual, Potential]
  )(using TS: TypeShow[Potential]) extends Checker[Actual, Not[Potential]] {
    inline def confine(actual: Actual): ValidatedNel[FailedCheck, Actual & Not[Potential]] = 
      inner.confine(actual).fold(
        list => Validated.valid(actual.asInstanceOf[Actual & Not[Potential]]),
        a => Validated.invalidNel(s"${a} was ${TS.show}")
      )
  }

  inline def apply[From, To](
    check: From => Boolean
  )(using ev: From =:= (From & To)) = {
    new Checker[From, To] {
      def confine(actual: From) = {
        Validated.condNel(check(actual), ev(actual), FailedCheck.explain(
          s"'${actual}' not valid"
        ))
      }
    }
  }
}