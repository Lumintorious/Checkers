package checkers.macros

import quoted.*
import checkers.*
import util.chaining.*

object IntrospectorMacros {
  inline def makeBasic[T]: Introspector[T] =
    ${ makeBasicMacro[T] }

  def makeBasicMacro[T : Type](using quotes: Quotes): Expr[Introspector[T]] = {
    IntrospectorMacros().makeBasic[T]
  }

  inline def makeForProduct[T <: Product]: Introspector[T] =
    ${ makeForProductMacro[T] }

  def makeForProductMacro[T <: Product : Type](using quotes: Quotes): Expr[Introspector[T]] = {
    IntrospectorMacros().makeForProduct[T]
  }
}

class IntrospectorMacros(using val quotes: Quotes) {
  import quotes.reflect.*
  private val reporter = MacroReporter()

  def makeForProduct[T <: Product : Type]: Expr[Introspector[T]] = {
    val concreteTypes = findConcreteTypes(TypeRepr.of[T])
    if(concreteTypes.length != 1) {
      reporter.noConcreteType[T]
    } else {
      concreteTypes.head.asType.pipe { case '[cType] =>
        makeForProductWithConcrete[T, cType]
      }
    }
  }

  def makeForProductWithConcrete[T <: Product : Type, Concrete : Type]: Expr[Introspector[T]] = {
    val typeRepr = TypeRepr.of[T]
    val concreteRepr = TypeRepr.of[Concrete]
    
    val productSymbol =
      concreteRepr
        .classSymbol
        .filter(_.caseFields.length > 0)
        .getOrElse(reporter.notProduct[Concrete])

    val fieldsAndTypes = productSymbol.caseFields.map { symbol =>
      (symbol,
        (if symbol.isValDef
        then symbol.tree.asInstanceOf[ValDef].tpt.tpe
        else TypeRepr.of[Any]).asType
      )
    }
    val doChecks = fieldsAndTypes.map { case (symbol, '[f]) =>
      val introspector = summonExpr[Introspector[f]] match {
        case Right(expr) => expr
        case Left(explanation) => reporter.introspectorNotFound[Introspector[f]](explanation)
      }
      '{
        (t: T) => {
          val fieldName = ${Expr(symbol.name)}
          val fieldValue = ${Select('{t}.asTerm, symbol).asExprOf[f]}
          val checked = ${introspector}.checkSelf(fieldValue)
          checked match {
            case Right(f) => List.empty
            case Left(failures) =>
              failures.map(_.reportedInto(fieldName))
          }
        }
      }
    }

    val doChecksExpr = Expr.ofList(doChecks)

    '{
      (obj: T) =>
        val failures = ${doChecksExpr}.flatMap(_.apply(obj))
        Checker.combine(
          obj.asInstanceOf[Concrete].checkAs[T],
          Either.cond(failures.isEmpty, obj, failures)
        )
    }
  }

  def makeBasic[T : Type]: Expr[Introspector[T]] = {
    val concreteTypes = findConcreteTypes(TypeRepr.of[T])
    if(concreteTypes.length != 1) {
      reporter.noConcreteType[T]
    } else {
      concreteTypes.head.asType.pipe { case '[cType] =>
        makeBasicWithConcrete[T, cType]
      }
    }
  }

  def makeBasicWithConcrete[T : Type, Concrete : Type]: Expr[Introspector[T]] = {
    val typeRepr = TypeRepr.of[T]
    val concreteRepr = TypeRepr.of[Concrete]
    '{
      (obj: T) => obj.asInstanceOf[Concrete].checkAs[T]
    }
  }
}
