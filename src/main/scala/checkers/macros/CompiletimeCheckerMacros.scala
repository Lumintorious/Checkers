package checkers.macros

import scala.quoted.*
import checkers.Guarantee

object CompiletimeCheckerMacros {

  def from[T: Type, R: Type](f: Expr[T => R])(using Quotes): Expr[T] => Expr[R] =
    (x: Expr[T]) => '{ $f($x) }

  inline def checkAtCompiletime[Out](
    inline condition: Int => Boolean,
    inline in: Int,
    inline out: => Out,
  ): Out =
    ${ checkAtCompiletimeMacro[Out]('condition, 'in, 'out) }

  def checkAtCompiletimeMacro[Out : Type](
    condition: Expr[Int => Boolean],
    in: Expr[Int],
    out: Expr[Out],
  )(using quotes: Quotes): Expr[Out] = {
    import quotes.reflect.*
    val compiletimeCondition = from(condition)
    val input = in.value.getOrElse(report.errorAndAbort("Input not visible at compiletime"))
    val result = compiletimeCondition(in)

    result.value match {
      case None =>
        report.errorAndAbort("Output not visible at compiletime: \n" + result.asTerm.show)
      case Some(true) =>
        out
      case Some(false) =>
        report.errorAndAbort("Not valid")
    }
  }
}
