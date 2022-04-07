package checkers.macros

import scala.quoted.*
import checkers.*
import cats.data.{Validated, ValidatedNel}
import cats.data.NonEmptyList
import scala.reflect.TypeTest

object ConfinementAccumulator {

  inline def accumulate[Actual, Target]: Checker[Actual, Target] =
    ${ accumulateImpl[Actual, Target] }

  def accumulateImpl[Actual : Type, Target : Type](using quotes: Quotes): Expr[Checker[Actual, Target]] =
    val x = ConfinementAccumulator()
    import x.quotes.reflect.*
    try {
      x.accumulateFor(
        TypeRepr.of[Actual],
        TypeRepr.of[Target]
      ).asExpr.asExprOf[Checker[Actual, Target]]
    } catch {
      case e: Throwable =>
        e.printStackTrace()
        throw e

    }

  inline def spliceOwnerName: String = ${spliceOwnerNameImpl()}

  def spliceOwnerNameImpl()(using quotes: Quotes): Expr[String] =
    import quotes.reflect.*

    Expr(Symbol.spliceOwner.owner.owner.name)
}


class ConfinementAccumulator(using val quotes: Quotes) {
  import quotes.reflect.*

  private type T = TypeRepr

  def nameOf(repr: T): String = {
    repr match {
      case ConstantType(const) =>
        const.value.toString
      case AppliedType(base, params) =>
        base.typeSymbol.name + params.map(nameOf).mkString("[", ", ", "]")
      case TypeRef(repr, name) => name
      case repr => repr.show
    }
  }

  def reportConfinementNotFound(in: T, out: T) = {
    val e = Exception()
    report.warning(e.getStackTrace().toList.take(5).mkString("\n"))
    report.errorAndAbort(
      s""" 
        Cannot confine ${nameOf(in)}
        to ${nameOf(out)}. 
        Missing Checker.
      """
    )
  }

  def confinementFor(in: T, out: T): Option[Term] =
    Implicits.search(TypeRepr.of[Checker].appliedTo(List(in, out))) match {
      case success: ImplicitSearchSuccess => Some(success.tree)
      case failure: ImplicitSearchFailure => None
    }

  def typeShowFor(out: T): Option[Term] =
    Implicits.search(TypeRepr.of[TypeShow].appliedTo(out)) match {
      case success: ImplicitSearchSuccess => Some(success.tree)
      case failure: ImplicitSearchFailure => None
    }

  def messageFor(out: T): Option[Term] =
    Implicits.search(TypeRepr.of[BrokenConfinementMessage].appliedTo(out)) match {
      case success: ImplicitSearchSuccess => Some(success.tree)
      case failure: ImplicitSearchFailure => None
    }

  def attemptToGiveMessage(in: T, out: T, checkers: Term): Term =
    (in.asType, out.asType, messageFor(out)) match {
      case ('[inT], '[outT], Some(message)) => '{
        val prev: Checker[inT, outT] = ${checkers.asExpr.asExprOf[Checker[inT, outT]]}
        new Checker[inT, outT] {
          def confine(actual: inT): ValidatedNel[FailedCheck, inT & outT] = {
            prev.confine(actual).leftMap(l => NonEmptyList.of(l.map(
              broken => 
                if(broken.isBasic) {
                  broken.copy(explanation = 
                    ${message.asExpr.asExprOf[BrokenConfinementMessage[outT]]}.format(actual),
                    isBasic = false
                  )
                } else {
                  broken
                }
              )
            ).head)
          }
        }
      }.asTerm
      case ('[inT], '[outT], _) => '{
        val prev: Checker[inT, outT] = ${checkers.asExpr.asExprOf[Checker[inT, outT]]}
        new Checker[inT, outT] {
          def confine(actual: inT): ValidatedNel[FailedCheck, inT & outT] =
            prev.confine(actual).leftMap(l => NonEmptyList.of(l.map(
            broken =>
              if(broken.isBasic) {
                broken.copy(explanation = 
                  s"'${actual}' is not ${${Expr(TypeShow.getForRepr(out.asInstanceOf))}}",
                  isBasic = false
                )
              } else {
                broken
              }
            )
            ).head)
        }
      }.asTerm
      case _ => checkers
    }

  def accumulateSimpleType(in: T, out: T): Term = {
    val result: Term = confinementFor(in, out) match {
      case Some(term) => {
        term
      }
      case None => {
        (in.asType, out.asType, out) match {
          case (_, _, out: AppliedType) => {
            reportConfinementNotFound(in, out)
          }
          case ('[inT], '[outT], out) if out.classSymbol.isDefined => '{
            new Checker[inT, outT] {
              def confine(actual: inT) =
                val name = ${Expr(out.show)}
                Validated.condNel(
                  actual.isInstanceOf[outT],
                  actual.asInstanceOf[inT & outT],
                  FailedCheck.explain(s"'${actual}' was not an instance of ${name}")
                    .copy(isBasic = true)
                )
            }
          }.asTerm
          case _ => {
            reportConfinementNotFound(in, out)
          }
        }
      }
    }
    attemptToGiveMessage(in, out, result)
  }
    

  def accumulateOpaqueTypeRef(in: T, out: TypeRef): Term =
    // confinementFor(in, out).getOrElse(reportConfinementNotFound(in, out))
    accumulateSimpleType(in, out)

  def accumulateTypeRef(in: T, out: TypeRef): Term =
    val previous = accumulateOpaqueTypeRef(in, out)
      // if(out == out.dealias) {
      //   accumulateSimpleType(in, out)
      // } else {
      //   accumulateFor(in, out.dealias)
      // }
    
    // val result = (in.asType, out.asType, out.dealias.asType) match {
    //   case ('[inT], '[outT], '[dealiasedOutT]) => '{
    //     val prevConfinement: Checker[inT, outT] =
    //       ${previous.asExpr.asExprOf[Checker[inT, outT]]}
    //     val combined = prevConfinement.widen[outT]
    //     combined
    //   }.asTerm
    // }
    attemptToGiveMessage(in, out, previous)

  def accumulateAndType(in: T, out: AndType): Term =
    val left  = accumulateFor(in, out.left)
    val right = accumulateFor(in, out.right)

    (in.asType, out.left.asType, out.right.asType) match {
      case ('[inT], '[leftT], '[rightT]) => '{
        val leftConfinement: Checker[inT, leftT] =
          ${left.asExpr.asExprOf[Checker[inT, leftT]]}
        val rightConfinement: Checker[inT, rightT] =
          ${right.asExpr.asExprOf[Checker[inT, rightT]]}
        val combined = (leftConfinement && rightConfinement)
        combined
      }.asTerm
    }

  def accumulateOrType(in: T, out: OrType): Term =
    val left  = accumulateFor(in, out.left)
    val right = accumulateFor(in, out.right)

    (in.asType, out.left.asType, out.right.asType) match {
      case ('[inT], '[leftT], '[rightT]) => '{
        val leftConfinement: Checker[inT, leftT] =
          ${left.asExpr.asExprOf[Checker[inT, leftT]]}
        val rightConfinement: Checker[inT, rightT] =
          ${right.asExpr.asExprOf[Checker[inT, rightT]]}
        val combined = (leftConfinement || rightConfinement)
        combined
      }.asTerm
    }

  def accumulateNotType(in: T, notType: AppliedType): Term =
    val inner = accumulateFor(in, notType.args.head)

    (in.asType, notType.args.head.asType) match {
      case ('[inT], '[notT]) => '{
        val innerConfinement: Checker[inT, notT] =
          ${inner.asExpr.asExprOf[Checker[inT, notT]]}
        innerConfinement.negated
      }.asTerm
    }

  def accumulateFor(in: T, out: T): Term =
    val result = out match {
      case and: AndType => accumulateAndType(in, and)
      case and: OrType => accumulateOrType(in, and)
      case not: AppliedType if not.tycon =:= TypeRepr.of[Not] => accumulateNotType(in, not)
      case ref: TypeRef if ref.isOpaqueAlias && ref.classSymbol.isEmpty => accumulateOpaqueTypeRef(in, ref)
      case ref: TypeRef => accumulateTypeRef(in, ref)
      case _ => accumulateSimpleType(in, out)
    }
    result
}
