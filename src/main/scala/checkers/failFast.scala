package checkers

import scala.util.control.ControlThrowable

final class FailFastCtx()
final class FailFastFailed(val status: List[FailedCheck]) extends ControlThrowable
inline given failFastConversion[From, To](using AV: FailFastCtx, C: Checker[From, To]): Conversion[From, From & To] =
  f =>
    C.confine(f) match {
      case Right(fAndT) => fAndT
      case Left(reasons) => throw FailFastFailed(reasons)
    }

inline def failFast[T](block: FailFastCtx ?=> T): Checked[T] =
  try {
    Right(block(using FailFastCtx()))
  } catch {
    case e: FailFastFailed => Left(e.status)
  }


