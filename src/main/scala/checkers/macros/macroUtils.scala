package checkers.macros

import quoted.*

def summonExpr[T : Type](using quotes: Quotes): Either[String, Expr[T]] = {
  import quotes.reflect.*
  Implicits.search(TypeRepr.of[T]) match {
    case success: ImplicitSearchSuccess =>
      Right(success.tree.asExprOf[T])
    case failure: ImplicitSearchFailure =>
      Left(failure.explanation)
  }
}

inline def concreteTypeOf[T]: List[String] =
  ${ concreteTypeOfMacro[T] }

def concreteTypeOfMacro[T : Type](using quotes: Quotes): Expr[List[String]] = {
  import quotes.reflect.*
  val repr = TypeRepr.of[T]
  Expr(findConcreteTypes(repr).map(_.show))
}

def findConcreteTypes(using quotes: Quotes)(repr: quotes.reflect.TypeRepr): List[quotes.reflect.TypeRepr] = {
  import quotes.reflect.*
  try {
    repr.dealias match {
      case ref: Refinement => List.empty
      case ref: TermRef => findConcreteTypes(ref.widen)
      case and: AndType => findConcreteTypes(and.left) ++ findConcreteTypes(and.right)
      case app: AppliedType =>
        findConcreteTypes(app.tycon).map(_.appliedTo(app.args))
      case ref: TypeRef => {
        val tpe = ref.translucentSuperType.dealias
        if(tpe.classSymbol.isDefined && tpe.classSymbol != TypeRepr.of[Any].classSymbol) {
          List(ref)
        } else {
          List.empty
        }
      }
      case _ =>
        List(repr)
    }
  } catch case e: Throwable => List.empty
}