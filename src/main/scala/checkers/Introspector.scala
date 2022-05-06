package checkers

import scala.util.NotGiven
import checkers.macros.*
import io.circe.Decoder
import io.circe.Encoder
import scala.annotation.targetName
import compiletime.*

trait Introspector[T] {
  def checkSelf(self: T): Checked[T]
}

object Introspector {
  inline def checkLevel[T](t: T): Checked[T] =
    IntrospectorMacros.makeBasic[T].checkSelf(t)

  inline given basic[T <: (String | Int | Char)]: Introspector[T] =
    IntrospectorMacros.makeBasic[T]
    
  inline def derived[T <: Product]: Introspector[T] =
    IntrospectorMacros.makeForProduct[T]
    

  // inline given str: SelfChecker[String] =
  //   s => macros.SelfCheckMacros.check[String](s)

  // inline given product[T <: Product](using NotGiven[SelfChecker[T]]): SelfChecker[T] =
  //   t => macros.SelfCheckMacros.checkTry[T](t)
    
  // inline given [A, B]: SelfChecker[A & B] =
  //   t => macros.SelfCheckMacros.checkTry[A & B](t)
}