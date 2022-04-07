package checkers

import cats.data.{Validated, ValidatedNel}

final case class FailedCheck(
  accessPoints: List[String],
  explanation: String,
  isBasic: Boolean = true
) {
  def reportedInto(field: String): FailedCheck =
    this.copy(accessPoints = field :: accessPoints)

  def path: String =
    accessPoints.mkString(".")

  override def toString: String =
    s"${path}${if accessPoints.isEmpty then "" else ": "}${explanation}"
}

object FailedCheck {
  inline def explain(explanation: String) =
    FailedCheck(List.empty, explanation)

  inline given Conversion[String, FailedCheck] =
    explain(_)
}

trait BrokenConfinementMessage[X] {
  def format(x: Any): String
}

type IfBroken[X] = BrokenConfinementMessage[X]

object BrokenConfinements {
  transparent inline def unapplySeq(validated: ValidatedNel[FailedCheck, ?]): List[FailedCheck] =
    validated match
      case Validated.Valid(_) => List()
      case Validated.Invalid(err) => err.toList
}