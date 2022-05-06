package checkers
package definitions

import quoted.*
import scala.collection.IterableFactory
import scala.collection.MapFactory

object collectionMacros {
  transparent inline def exprFromType[T]: Any =
    ${ exprFromTypeMacro[T] }

  def exprFromTypeMacro[T : Type](using quotes: Quotes): Expr[Any] = {
    import quotes.reflect.*

    TypeRepr.of[T] match {
      case ref @ TermRef(_, term) =>
        ref.termSymbol.tree match {
          case ValDef(_, _, Some(rhs)) =>
            return rhs.asExpr
          case _ =>
            report.errorAndAbort("Type is not of a val")
        }
      case ConstantType(StringConstant(s)) =>
        Expr(s).asExprOf[T]
      case ConstantType(IntConstant(s)) =>
        Expr(s).asExprOf[T]
      case _ => report.errorAndAbort("Bottom case")
    }
  }

  transparent inline def getSizeOf[S]: Int =
    ${ getSizeOfMacro[S] }

  def getSizeOfMacro[S : Type](using quotes: Quotes): Expr[Int] = {
    import quotes.reflect.*

    Expr(exprFromTypeMacro[S] match {
      case '{ (${x}: IterableFactory[?])(${Varargs(things)}: _*) } => things.length
      case '{ (${x}: MapFactory[?])(${Varargs(things)}: _*) } => things.length
      case '{ ${s}: String } => s.value.get.length
      case _ =>
        report.errorAndAbort("Cannot deduce length")
    })
  }

  transparent inline def sizeOfGuarantee[S, L <: Int]: Guarantee[S, SizeOf[L]] =
    ${ guaranteeSizeMacro[S, L] }

  def guaranteeSizeMacro[S : Type, L <: Int : Type](using quotes: Quotes): Expr[Guarantee[S, SizeOf[L]]] = {
    import quotes.reflect.*

    val valueL = exprFromTypeMacro[L].asExprOf[Int].value.get
    val seqL = getSizeOfMacro[S].value.get

    if(valueL == seqL) {
      '{ new Guarantee[S, SizeOf[L]](_.asInstanceOf[S & SizeOf[L]]) }
    } else {
      report.errorAndAbort("Sizes are not equal")
    }
  }

  transparent inline def sizeLTGuarantee[S, L <: Int]: Guarantee[S, SizeLT[L]] =
    ${ sizeLTGuaranteeMacro[S, L] }

  def sizeLTGuaranteeMacro[S : Type, L <: Int : Type](using quotes: Quotes): Expr[Guarantee[S, SizeLT[L]]] = {
    import quotes.reflect.*

    val valueL = exprFromTypeMacro[L].asExprOf[Int].value.get
    val seqL = getSizeOfMacro[S].value.get

    if(seqL < valueL) {
      '{ new Guarantee[S, SizeLT[L]](_.asInstanceOf[S & SizeLT[L]]) }
    } else {
      report.errorAndAbort("Sizes are not equal")
    }
  }

  transparent inline def sizeGTGuarantee[S, L <: Int]: Guarantee[S, SizeGT[L]] =
    ${ sizeGTGuaranteeMacro[S, L] }

  def sizeGTGuaranteeMacro[S : Type, L <: Int : Type](using quotes: Quotes): Expr[Guarantee[S, SizeGT[L]]] = {
    import quotes.reflect.*

    val valueL = exprFromTypeMacro[L].asExprOf[Int].value.get
    val seqL = getSizeOfMacro[S].value.get

    if(seqL > valueL) {
      '{ new Guarantee[S, SizeGT[L]](_.asInstanceOf[S & SizeGT[L]]) }
    } else {
      report.errorAndAbort("Sizes are not equal")
    }
  }
}
