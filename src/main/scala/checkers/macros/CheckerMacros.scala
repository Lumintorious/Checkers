package checkers.macros

import quoted.*
import checkers.*
import util.chaining.*

object CheckerMacros {
  inline def make[Actual, Potential]: Checker[Actual, Potential] =
    ${ makeMacro[Actual, Potential] }

  def makeMacro[Actual : Type, Potential : Type](using quotes: Quotes): Expr[Checker[Actual, Potential]] = {
    CheckerMacros().makeChecker[Actual, Potential]
  }
}

class CheckerMacros(using val quotes: Quotes) {
  import quotes.reflect.*
  private val reporter = MacroReporter()

  def addMessage[A : Type, B : Type](checker: Expr[Checker[A, B]]): Expr[BuiltChecker[A, B]] = {
    summonExpr[IfBroken[B]] match {
      case Right(ifBroken) =>
        '{ ${checker}.ifBroken(${ifBroken}.format) }
      case Left(_) =>
        '{ ${checker}.built }
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

  def makeCheckerTowardsRefinement[A : Type, R : Type]: Expr[Checker[A, R]] = {
    val rRepr = TypeRepr.of[R]
    val fieldMappings = mappedRefinementFields[A, R]
    val makeFailures =
      for case RefinementMappings(
        name, '[rfType],
        field, '[fType]
      ) <- fieldMappings
      yield '{ (a: A) => {
        val fieldValue = ${Select('{a}.asTerm, field).asExprOf[fType]}
        val checked = fieldValue.checkAs[rfType]
        checked match {
          case Right(field) =>
            List.empty
          case Left(failures) =>
            failures.map(_.reportedInto(${Expr(name)}))
        }
      }}

    val makeFailuresExpr = Expr.ofList(makeFailures)

    '{ (a: A) =>
      val allFailures = ${makeFailuresExpr}.flatMap(_.apply(a))
      Either.cond(allFailures.length == 0, a.asInstanceOf[A & R], allFailures)
    }
  }

  def makeCheckerTowardsArbitrary[A : Type, B : Type]: Expr[Checker[A, B]] = {
    summonExpr[Checker[A, B]] match {
      case Right(checkerExpr) =>
        '{ ${checkerExpr}.ifBroken(a => s"'${a}' is not ${TypeShow.getFor[B]}") }
      case Left(explanation) =>
        reporter.checkerNotFound[Checker[A, B]](explanation)
    }
  }

  def makeCheckerTowardsAndType[A : Type, And : Type](andRepr: AndType): Expr[Checker[A, And]] = {
    (andRepr.left.asType, andRepr.right.asType).pipe { case ('[l], '[r]) =>
      val leftChecker:  Expr[Checker[A, l]] = makeChecker[A, l]
      val rightChecker: Expr[Checker[A, r]] = makeChecker[A, r]

      '{ ${leftChecker} && ${rightChecker} }.asExprOf[Checker[A, And]] 
    }
  }

  def makeCheckerTowardsOrType[A : Type, Or : Type](orRepr: OrType): Expr[Checker[A, Or]] = {
    (orRepr.left.asType, orRepr.right.asType).pipe { case ('[l], '[r]) =>
      val leftChecker:  Expr[Checker[A, l]] = makeChecker[A, l]
      val rightChecker: Expr[Checker[A, r]] = makeChecker[A, r]

      '{ ${leftChecker} || ${rightChecker} }.asExprOf[Checker[A, Or]] 
    }
  }

  def makeCheckerTowardsSingletonType[A : Type, S : Type]: Expr[Checker[A, S]] = {
    val sName = Expr(TypeRepr.of[S].show)
    summonExpr[ValueOf[S]] match {
      case Right(expr) => '{ (a: A) =>
        val expected = ${expr}.value
        Either.cond(
            a.isInstanceOf[S] && a == expected,
            a.asInstanceOf[A & S],
            List(s"${a} is not ${expected}: ${$sName}")
        )
      }
      case Left(explanation) =>
        reporter.checkerNotFound[S](explanation)
    }
  }

  def makeChecker[A : Type, B : Type]: Expr[Checker[A, B]] = {
    val actualRepr = TypeRepr.of[A]
    val potentialRepr = TypeRepr.of[B]
    val result = potentialRepr match {
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
