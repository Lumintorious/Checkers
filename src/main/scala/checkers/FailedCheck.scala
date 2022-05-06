package checkers

final case class FailedCheck(
  accessPoints: List[String],
  explanation: String,
  isBasic: Boolean = true
) {
  def reportedInto(field: String): FailedCheck =
    if(!field.trim.isEmpty) {
      this.copy(accessPoints = field :: accessPoints)
    } else {
      this
    }

  def path: String =
    accessPoints.mkString(".")

  def purgePath =
    this.copy(accessPoints = List.empty)

  override def toString: String =
    s"${path}${if accessPoints.isEmpty then "" else ": "}${explanation}"
}

object FailedCheck {
  inline def explain(explanation: String) =
    FailedCheck(List.empty, explanation)

  inline given Conversion[String, FailedCheck] =
    explain(_)
}

extension [E, E2, A](self: Either[E, Either[E2, A]]) {
  def mergeLeft = 
    self.fold(Left(_), identity)
}

trait IfBroken[X] {
  def format(x: Any): String
}

object BrokenConfinements {
  transparent inline def unapplySeq(validated: Checked[?]): List[FailedCheck] =
    validated match
      case Right(_) => List()
      case Left(err) => err.toList
}