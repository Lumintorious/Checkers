package checkers.macros

import quoted.*
import checkers.*
import util.chaining.*
import checkers.typeclasses.*

object CheckerMacros {
  inline def make[F[_, _], Actual, Potential](using b: Both[F], c: CheckerLike[F], r: RenameError[F]): F[Actual, Potential] =
    ${ makeMacro[F, Actual, Potential](using ('b), ('c), ('r)) }

  def makeMacro[F[_, _] : Type, Actual : Type, Potential : Type](using both: Expr[Both[F]], c: Expr[CheckerLike[F]], r: Expr[RenameError[F]])(using quotes: Quotes): Expr[F[Actual, Potential]] = {
    CheckerMacros[F]().makeChecker[Actual, Potential]
  }
}

class CheckerMacros[F[_, _] : Type](using val quotes: Quotes, both: Expr[Both[F]], c: Expr[CheckerLike[F]], r: Expr[RenameError[F]]) {
  import quotes.reflect.*
  private val reporter = MacroReporter()

  def addMessage[A : Type, B : Type](checker: Expr[F[A, B]]): Expr[F[A, B]] = {
    summonExpr[IfBroken[B]] match {
      case Right(ifBroken) =>
        '{ ${r}.rename[A, B](${checker})(${ifBroken}.format) }
      case Left(_) =>
        checker
    }
  }

  final case class RefinementMappings(
    fieldName: String,
    refinementFieldType: Type[?],
    concreteFieldSymbol: Symbol,
    concreteFieldType: Type[?]
  )

  def namesAndTypesOfRefinement(ref: TypeRepr): List[(String, TypeRepr, Type[?])] = {
    ref match {
      case ref: Refinement =>
        ((ref.name, ref.info, ref.info.asType)) +: namesAndTypesOfRefinement(ref.parent)
      case _ =>
        List.empty
    }
  }

  def mappedRefinementFields[T : Type, R : Type]: List[RefinementMappings] = {
    val tRepr = TypeRepr.of[T]
    val tSymbol = tRepr.typeSymbol

    namesAndTypesOfRefinement(TypeRepr.of[R]).map { case (name, fRepr, fType) =>
      val field = tSymbol.declaredField(name)
      val fieldRepr = field.tree.asInstanceOf[ValDef].tpt.tpe

      RefinementMappings(
        name, fType,
        field, fieldRepr.asType
      )
    }
  }

  def makeCheckerTowardsRefinement[A : Type, R : Type]: Expr[F[A, R]] = {
    val rRepr = TypeRepr.of[R]
    val fieldMappings = mappedRefinementFields[A, R]
    val makeFailures =
      for case RefinementMappings(
        name, '[rfType],
        field, '[fType]
      ) <- fieldMappings
      yield '{ (a: A) => {
        val fieldValue = ${Select(('{a}).asTerm, field).asExprOf[fType]}
        val checked = fieldValue.checkAs[rfType]
        checked match {
          case Right(field) =>
            List.empty
          case Left(failures) =>
            failures.map(_.reportedInto(${Expr(name)}))
          case _ => throw Exception("Weird case branch on enums.")
        }
      }}

    val makeFailuresExpr = Expr.ofList(makeFailures)

    '{
      ${c}.make((a: A) =>
        val allFailures = ${makeFailuresExpr}.flatMap(_.apply(a))
        Either.cond(allFailures.length == 0, a.asInstanceOf[A & R], allFailures))
    }
  }

  def makeCheckerTowardsArbitrary[A : Type, B : Type]: Expr[F[A, B]] = {
    summonExpr[F[A, B]] match {
      case Right(checkerExpr) =>
        '{ ${r}.rename[A, B](${checkerExpr})(a => s"'${a}' is not ${TypeShow.getFor[B]}") }
      case Left(explanation) =>
        reporter.checkerNotFound[F[A, B]](explanation)
    }
  }

  def makeCheckerTowardsAndType[A : Type, And : Type](andRepr: AndType): Expr[F[A, And]] = {
    (andRepr.left.asType, andRepr.right.asType).pipe { case ('[l], '[r]) =>
      val leftChecker:  Expr[F[A, l]] = makeChecker[A, l]
      val rightChecker: Expr[F[A, r]] = makeChecker[A, r]

      '{ ${both}.both(${leftChecker}, ${rightChecker}) }.asExprOf[F[A, And]] 
    }
  }

  def makeCheckerTowardsOrType[A : Type, Or : Type](orRepr: OrType): Expr[F[A, Or]] = {
    (orRepr.left.asType, orRepr.right.asType).pipe { case ('[l], '[r]) =>
      val leftChecker:  Expr[F[A, l]] = makeChecker[A, l]
      val rightChecker: Expr[F[A, r]] = makeChecker[A, r]

      report.warning("OR PLACEHOLDER AS AND")
      '{ ${both}.both(${leftChecker}, ${rightChecker}) }.asExprOf[F[A, Or]] 
    }
  }

  def makeCheckerTowardsSingletonType[A : Type, S : Type]: Expr[F[A, S]] = {
    val sName = Expr(TypeRepr.of[S].show)
    summonExpr[ValueOf[S]] match {
      case Right(expr) => '{ 
        ${c}.make { (a: A) =>
          val expected = ${expr}.value
          Either.cond(
              a.isInstanceOf[S] && a == expected,
              a.asInstanceOf[A & S],
              List(s"${a} is not ${expected}: ${$sName}")
          )

        }
      }
      case Left(explanation) =>
        reporter.checkerNotFound[S](explanation)
    }
  }

  def makeCheckerTowardsTypeAlias[A : Type, Alias : Type]: Expr[F[A, Alias]] = {
    TypeRepr.of[Alias].dealias.asType.pipe { case '[dealiased] =>
      makeChecker[A, dealiased].asExprOf[F[A, Alias]]
    }
  }

  def makeChecker[A : Type, B : Type]: Expr[F[A, B]] = {
    val actualRepr = TypeRepr.of[A]
    val potentialRepr = TypeRepr.of[B]
    
    val result = potentialRepr.dealias match {
      case anyRepr if anyRepr != anyRepr.dealias =>
        makeCheckerTowardsTypeAlias[A, B]
      case ref: Refinement => // { val x: X }
        makeCheckerTowardsRefinement[A, B]
      case and: AndType => // X & Y
        makeCheckerTowardsAndType[A, B](and)
      case or: OrType => // X | Y
        makeCheckerTowardsOrType[A, B](or)
      case s: TypeRepr if s.isSingleton => // x.type
        makeCheckerTowardsSingletonType[A, B]
      case ref: TermRef => // x.type
        makeCheckerTowardsSingletonType[A, B]
      case arbitrary => // X, X[Y]
        makeCheckerTowardsArbitrary[A, B]
    }
    addMessage[A, B](result)
  }
}
