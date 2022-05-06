package checkers

import compiletime.ops.int.*
import compiletime.*
import checkers.typeclasses.*
import scala.annotation.implicitNotFound
import scala.util.NotGiven

@implicitNotFound("Cannot guarantee that ${A} is a ${B}")
class Guarantee[A, B](turn: A => (A & B) = identity) {
  inline def convert(a: A): A & B = turn(a)
}

object Guarantee {
  inline given automatically[A <: Singleton, B <: Any](using inline G: Guarantee[A, B], NC: NotGiven[B <:< Checked[?]]): Conversion[A, A & B] =
    a => 
      // summonInline[Guarantee[a.type, B]]
      G.convert(a).asInstanceOf[A & B]

  inline def apply[A, B](inline fn: => Boolean = true)(using ev: (A =:= (A & B))): Guarantee[A, B] =
    inline if fn
    then new Guarantee(ev)
    else error("Cannot guarantee right hand side is of expected type")

  inline given synthesize[A, B, C]: Guarantee[A, B & C] =
    macros.CheckerMacros.make[Guarantee, A, B & C]
  
  inline given Both[Guarantee] with {
    inline def both[A, B, C](f1: Guarantee[A, B], f2: Guarantee[A, C]): Guarantee[A, B & C] =
      new Guarantee(_.asInstanceOf[A & B & C])
  }

  inline given CheckerLike[Guarantee] with {
    inline def make[A, S](fn: A => Either[List[FailedCheck], A & S]): Guarantee[A, S] = {
      new Guarantee(_.asInstanceOf[A & S])
    }
  }

  inline given RenameError[Guarantee] with {
    def rename[A, B](f: Guarantee[A, B])(rename: A => FailedCheck): Guarantee[A, B] = {
      f
    }
  }
}

extension [A <: Singleton] (inline self: A) {
  inline def to[B](using G: Guarantee[A, B]): A & B =
    G.convert(self)
}

package compile {
  def test() = {
    // val x: Double & Less[10D] & DivisibleBy[2D] = 4D
    // val word: String & AlphaNumeric & SizeOf[5] = "horse"
    // println(word)
  }
}
