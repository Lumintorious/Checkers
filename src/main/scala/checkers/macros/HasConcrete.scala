package checkers.macros

import quoted.*
import util.chaining.*

trait HasConcrete[T] {
  type WholeType = T
  type ConcreteType 
}

object HasConcrete {
  transparent inline def getFor[T]: HasConcrete[T] = ${ getForMacro[T] }

  def getForMacro[T : Type](using quotes: Quotes): Expr[HasConcrete[T]] = {
    import quotes.reflect.*
    val whole = TypeRepr.of[T]
    val concr = findConcreteTypes(whole).head

    concr.asType.pipe { case '[concrT] =>
      '{
        new HasConcrete[T] {
          type ConcreteType = concrT
        }
      }
    }
  }
}
