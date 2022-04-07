package checkers

import compiletime.summonInline
import cats.data.{Validated, ValidatedNel}
import cats.Semigroupal
import cats.Functor

extension [T] (self: ValidatedNel[FailedCheck, T]) {
  def reportedAs(key: String): ValidatedNel[FailedCheck, T] = { self match
    case valid @ Validated.Valid(t) => valid
    case Validated.Invalid(list) => Validated.Invalid(
      list.map(_.reportedInto(key))
    )
  }

  def -> (key: String): ValidatedNel[FailedCheck, T] =
    self.reportedAs(key)
}

extension [Actual] (self: Actual) {
  inline def checkAs[P1]: ValidatedConfinement[Actual & P1] =
    macros.ConfinementAccumulator.accumulate[Actual, P1].confine(self)

  inline def checkIs[P1](using IV: Checker[Actual, P1]): Boolean =
    macros.ConfinementAccumulator.accumulate[Actual, P1].confine(self).isValid

}

package unsafe {
  
  extension [Actual] (self: Actual) {
    inline def trustAs[P1](using IV: Checker[Actual, P1]): Actual & P1 =
     IV.confine(self) match {
       case Validated.Valid(a) => a
       case Validated.Invalid(errs) => throw new IllegalArgumentException(
         "Unsafe trust failed: " + errs
       )
     }
  }
}

private type F[T] = ValidatedConfinement[T]
private type CF[T] = ValidatingInspace ?=> F[T]
private given ValidatingInspace = ValidatingInspace()
  
extension [A, Out](fn: (A) => Out) {
  transparent inline def checking[F[_]](fa: CF[A]) =
    fa.map(fn)
}
  
extension [A, B, Out](fn: (A, B) => Out) {
  transparent inline def checking(fa: CF[A], fb: CF[B]): (F[Out]) =
    Semigroupal.map2[F, A, B, Out](fa, fb)(fn)
}

extension [A, B, C, Out](fn: (A, B, C) => Out) {
  transparent inline def checking(fa: CF[A], fb: CF[B], fc: CF[C]): (F[Out]) =
    Semigroupal.map3[F, A, B, C, Out](fa, fb, fc)(fn)
}

extension [A, B, C, D, Out](fn: (A, B, C, D) => Out) {
  transparent inline def checking(fa: CF[A], fb: CF[B], fc: CF[C], fd: CF[D]): (F[Out]) =
    Semigroupal.map4(fa, fb, fc, fd)(fn)
}

extension [A, B, C, D, E, Out](fn: (A, B, C, D, E) => Out) {
  transparent inline def checking(fa: CF[A], fb: CF[B], fc: CF[C], fd: CF[D], fe: CF[E]): (F[Out]) =
    Semigroupal.map5(fa, fb, fc, fd, fe)(fn)
}

extension [A, B, C, D, E, G, Out](fn: (A, B, C, D, E, G) => Out) {
  transparent inline def checking(fa: CF[A], fb: CF[B], fc: CF[C], fd: CF[D], fe: CF[E], fg: CF[G]): (F[Out]) =
    Semigroupal.map6(fa, fb, fc, fd, fe, fg)(fn)
}
