package checkers

import scala.util.control.ControlThrowable
import cats.data.NonEmptyList
import cats.data.Validated

final class FailFastCtx()
final class FailFastFailed(val status: NonEmptyList[FailedCheck]) extends ControlThrowable
inline given failFastConversion[From, To](using AV: FailFastCtx, C: Checker[From, To]): Conversion[From, From & To] =
  f =>
    C.confine(f) match {
      case Validated.Valid(fAndT) => fAndT
      case Validated.Invalid(reasons) => throw FailFastFailed(reasons)
    }

inline def failFast[T](block: FailFastCtx ?=> T): ValidatedConfinement[T] =
  try {
    Validated.valid(block(using FailFastCtx()))
  } catch {
    case e: FailFastFailed => Validated.invalid(e.status)
  }


