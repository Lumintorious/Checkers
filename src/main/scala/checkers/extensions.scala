package checkers

import compiletime.summonInline
import cats.Semigroupal
import cats.Functor
import checkers.macros.CheckerMacros
import checkers.macros.TypeShow
import scala.annotation.targetName

extension [T] (self: Checked[T]) {
  def reportedAs(key: String): Checked[T] = { self match
    case valid @ Right(t) => valid
    case Left(list) => Left(
      list.map(_.reportedInto(key))
    )
  }

  // def  (key: String): Checked[T] =
  //   self.reportedAs(key)
}

extension [T] (self: T) {
  inline def checkAs[P1]: Checked[T & P1] =
    CheckerMacros.make[Checker, T, P1].confine(self)

  inline def checkAs_[P1](using I: Introspector[T & P1]): Checked[T & P1] =
    I.checkSelf(self.asInstanceOf[T & P1])

  inline def selfChecked(using SC: Introspector[T]): Checked[T] =
    SC.checkSelf(self)

  inline def checkIs[P1]: Boolean =
    self.checkAs[P1].isRight

}

package unsafe {
  
  extension [Actual] (self: Actual) {
    inline def trustAs[P1](using IV: Checker[Actual, P1]): Actual & P1 =
     IV.confine(self) match {
       case Right(a) => a
       case Left(errs) => throw new IllegalArgumentException(
         "Unsafe trust failed: " + errs
       )
     }
  }

  extension [Actual] (self: Actual) {
    inline def trustAs_[P1](using IV: BuiltChecker[Actual, P1]): Actual & P1 =
     IV.confine(self) match {
       case Right(a) => a
       case Left(errs) => throw new IllegalArgumentException(
         "Unsafe trust failed: " + errs
       )
     }
  }
}

private type F[T] = Checked[T]
private type CF[T] = ValidatingInspace ?=> F[T]
private given ValidatingInspace = ValidatingInspace()
  
extension [A, Out](fn: (A) => Out) {
  transparent inline def checking[F[_]](fa: CF[A]): Checked[Out] =
    fa.map(fn)
}

// extension [A, Out](fn: (A) => Out) {
//   transparent inline def checking[F[_]](fa: CF[A]): Checked[Out] =
//     fa.map(fn)
// }

// extension [A, Out](fn: (A) => Checked[Out]) {
//   @targetName("checkingFlatMapped")
//   inline def checking[F[_]](fa: CF[A]) =
//     fa.flatMap(fn)
// }
  
extension [A, B, Out](inline fn: (A, B) => Out) {
  inline def checking(fa: CF[A], fb: CF[B]): (F[Out]) =
    val params = TypeShow.showParamsNames(fn)
    Semigroupal.map2(fa(using ValidatingInspace()).reportedAs(params(0)), fb(using ValidatingInspace()).reportedAs(params(1)))(fn)
}

extension [A, B, C, Out](inline fn: (A, B, C) => Out) {
  inline def checking(fa: CF[A], fb: CF[B], fc: CF[C]): (F[Out]) =
    val params = TypeShow.showParamsNames(fn)
    Semigroupal.map3(
      fa(using ValidatingInspace()).reportedAs(params(0)),
      fb(using ValidatingInspace()).reportedAs(params(1)),
      fc(using ValidatingInspace()).reportedAs(params(2))
    )(fn)
}

extension [A, B, C, D, Out](inline fn: (A, B, C, D) => Out) {
  inline def checking(fa: CF[A], fb: CF[B], fc: CF[C], fd: CF[D]): (F[Out]) =
    val params = TypeShow.showParamsNames(fn)
    Semigroupal.map4(
      fa(using ValidatingInspace()).reportedAs(params(0)),
      fb(using ValidatingInspace()).reportedAs(params(1)),
      fc(using ValidatingInspace()).reportedAs(params(2)),
      fd(using ValidatingInspace()).reportedAs(params(3)),
    )(fn)
}

extension [A, B, C, D, E, Out](inline fn: (A, B, C, D, E) => Out) {
  inline def checking(fa: CF[A], fb: CF[B], fc: CF[C], fd: CF[D], fe: CF[E]): (F[Out]) =
    val params = TypeShow.showParamsNames(fn)
    Semigroupal.map5(
      fa.reportedAs(params(0)),
      fb.reportedAs(params(1)),
      fc.reportedAs(params(2)),
      fd.reportedAs(params(3)),
      fe.reportedAs(params(4)),
    )(fn)
}

