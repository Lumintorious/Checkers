package confinement

import cats.data.{Validated, ValidatedNel}

final case class BrokenConfinement(
  accessPoints: List[String],
  explanation: String,
  isBasic: Boolean = true
) {
  def reportedInto(field: String): BrokenConfinement =
    this.copy(accessPoints = field :: accessPoints)

  def path: String =
    accessPoints.mkString(".")

  override def toString: String =
    s"${path}${if accessPoints.isEmpty then "" else ": "}${explanation}"
}

object BrokenConfinement {
  inline def explain(explanation: String) =
    BrokenConfinement(List.empty, explanation)

  inline given Conversion[String, BrokenConfinement] =
    explain(_)
}

trait BrokenConfinementMessage[X] {
  def format(x: Any): String
}

type IfBroken[X] = BrokenConfinementMessage[X]

object BrokenConfinements {
  transparent inline def unapplySeq(validated: ValidatedNel[BrokenConfinement, ?]): List[BrokenConfinement] =
    validated match
      case Validated.Valid(_) => List()
      case Validated.Invalid(err) => err.toList
}