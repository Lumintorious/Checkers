package checkers

import checkers.macros.TypeShow
import scala.util.NotGiven

final class ValidatingInspace()

inline given autoConfine2[From, To](using VI: ValidatingInspace, C: BuiltChecker[From, To]): Conversion[From, Checked[From & To]] =
  f => f.checkAs[From & To]

inline given autoConfinePrimitive[From, To](using VI: ValidatingInspace, C: Checker[From, To]): Conversion[From, Checked[From & To]] =
  f => f.checkAs[From & To]

// inline given autoConfine[From, To](using VI: ValidatingInspace, I: Introspector[From & To], NG: NotGiven[Conversion[From, Checked[From & To]]]): Conversion[From, Checked[From & To]] =
//   f => I.checkSelf(f.asInstanceOf[From & To])

// inline given checkF[F[_], A, B](using I: Introspector[F[B]]): Checker[F[A], F[B]] =
//   f => I.checkSelf(f.asInstanceOf[F[B]])

// inline given autoConfineF[F[x], From, To](using VI: ValidatingInspace, I: Introspector[F[From & To]]): Conversion[F[From], Checked[F[From & To]]] =
//   f =>
//     I.checkSelf(f.asInstanceOf[F[From & To]])

given autoRight[T](using ValidatingInspace): Conversion[T, Either[Nothing, T]] = Right(_)


