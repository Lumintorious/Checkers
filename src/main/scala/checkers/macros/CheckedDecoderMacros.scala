package checkers.macros

import quoted.*
import io.circe.Decoder
import util.chaining.*
import checkers.Introspector
import io.circe.HCursor
import io.circe.DecodingFailure
import io.circe.Encoder
import io.circe.CursorOp.DownField
import io.circe.CursorOp.DownArray
import io.circe.CursorOp
import io.circe.ACursor
import io.circe.cursor.TopCursor
import io.circe.FailedCursor

object CheckedDecoderMacros {
  inline def make[T]: Decoder[T] =
    ${ makeMacro[T] }

  def makeMacro[T : Type](using quotes: Quotes): Expr[Decoder[T]] = {
    CheckedDecoderMacros().makeFromWidened[T]
  }

  inline def makeEncoder[T]: Encoder[T] =
    ${ makeEncoderMacro[T] }

  def makeEncoderMacro[T : Type](using quotes: Quotes): Expr[Encoder[T]] = {
    import quotes.reflect.*
    findConcreteTypes(TypeRepr.of[T]).head.asType.pipe { case '[c] =>
      summonExpr[Encoder[c]] match {
        case Right(expr) =>
          '{ (t: T) => ${expr}.asInstanceOf[Encoder[T]].apply(t) }
        case Left(explanation) =>
          report.errorAndAbort(explanation)
      }
    }
  }
}

class CheckedDecoderMacros(using val quotes: Quotes) {
  import quotes.reflect.*
  private val reporter = MacroReporter()
  
  def makeFromWidened[T : Type]: Expr[Decoder[T]] = {
    TypeRepr.of[T].widen.asType.pipe { case '[widenedT] =>
      make[widenedT].asExprOf[Decoder[T]]
    }
  }

  def make[T : Type]: Expr[Decoder[T]] = {
    val concreteTypes = findConcreteTypes(TypeRepr.of[T])
    if(concreteTypes.length != 1) {
      reporter.noConcreteType[T]
    } else {
      concreteTypes.head.asType.pipe { case '[cType] =>
        makeWithConcrete[T, cType]
      }
    }
  }
 
  def makeWithConcrete[T : Type, Concrete : Type]: Expr[Decoder[T]] = {
    summonExpr[Decoder[Concrete]] match {
      case Right(decoderExpr) =>
        summonExpr[Introspector[T]] match {
          case Right(introspectorExpr) =>
            makeUsingProps[T, Concrete](decoderExpr, introspectorExpr)
          case Left(explanation) =>
            report.errorAndAbort(explanation)
        }
      case Left(explanation) =>
        report.errorAndAbort(explanation)
    }
  }

  

  def makeUsingProps[T : Type, Concrete : Type](
    decoder: Expr[Decoder[Concrete]],
    introspector: Expr[Introspector[T]]
  ): Expr[Decoder[T]] = {
    '{
      val concreteDecoder = ${decoder}
      val tIntrospector = ${introspector}
      (c: HCursor) => {
        val concreteResult = concreteDecoder.apply(c)
        extension (cursor: HCursor) def isAllowedToSkipChecks: Boolean = {
          var current: ACursor = cursor

          while (!current.isInstanceOf[FailedCursor]) {
            if(current.isInstanceOf[CheckSkipPermissionCursor]) {
              return true
            }
            current = current.up
          }

          false
        }

        concreteResult match {
          case Right(concrete) => {
            val canSkipChecks = c.isAllowedToSkipChecks
            if(canSkipChecks) { // Top level cursor promises to introspect after decoding
              Right(concrete.asInstanceOf[T])
            } else {
              tIntrospector.checkSelf(concrete.asInstanceOf[T])
                .swap
                .map(l => DecodingFailure(l.mkString("; "), c.history))
                .swap
            }
          }
          case Left(err) => {
            Left(err)
          }
        }
      }
    }
  }
}
