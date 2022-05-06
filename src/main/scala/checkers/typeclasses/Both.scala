package checkers.typeclasses

import checkers.FailedCheck

trait CheckerLike[F[_, _]] {
  def make[A, S](fn: A => Either[List[FailedCheck], A & S]): F[A, S]
}

trait Both[F[_, _]] {
  def both[A, B, C](f1: F[A, B], f2: F[A, C]): F[A, B & C]

  extension [A, B, C](self: F[A, B]) def && (other: F[A, C]) = both(self, other)
  // extension [A, B, C](self: F[A, C]) def && (other: F[A, B]) = both(self, other)
}

trait RenameError[F[_, _]] {
  def rename[A, B](f: F[A, B])(rename: A => FailedCheck): F[A, B]
}

// object Both {

// }

// trait Both[F[_, _]] {
//   def both[A, B, C](f1: F[A, B], f2: F[A, C]): F[A, B & C]
// }