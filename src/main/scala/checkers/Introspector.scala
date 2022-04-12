package checkers

import scala.util.NotGiven
import checkers.macros.IntrospectorMacros

trait Introspector[T] {
  def checkSelf(self: T): Checked[T]
}

object Introspector {
  inline given basic[T]: Introspector[T] =
    IntrospectorMacros.makeBasic[T]
    
  // inline given str: SelfChecker[String] =
  //   s => macros.SelfCheckMacros.check[String](s)

  // inline given product[T <: Product](using NotGiven[SelfChecker[T]]): SelfChecker[T] =
  //   t => macros.SelfCheckMacros.checkTry[T](t)
    
  // inline given [A, B]: SelfChecker[A & B] =
  //   t => macros.SelfCheckMacros.checkTry[A & B](t)
}