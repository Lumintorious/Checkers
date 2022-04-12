// package checkers.macros

// import quoted.*
// import checkers.SelfChecker
// import checkers.Checked
// import checkers.checkAs
// import scala.annotation.meta.field
// import checkers.FailedCheck
// import checkers.Checker

// object SelfCheckMacros2 {
//   inline def makeBasic[T]: SelfChecker[T] = ${ makeBasicMacro[T] }
 
//   def makeBasicMacro[T : Type](using Quotes) =
//     SelfCheckMacros2().leveledSelfCheckerFor[T]

    
//   inline def makeForCaseClass[T <: Product]: SelfChecker[T] = ${ makeForCaseClassMacro[T] }

//   def makeForCaseClassMacro[T : Type](using Quotes) =
//     SelfCheckMacros2().selfCheckerForCaseClass[T]
// }

// class SelfCheckMacros2(using val quotes: Quotes) {
//   import quotes.reflect.*

//   def sh(repr: TypeRepr): String =
//     quotes.reflect.TypeReprMethods.show(repr)

//   def selfCheckerForBlob[T : Type]: Expr[SelfChecker[T]] = {
//     val repr = TypeRepr.of[T]

//     leveledSelfCheckerFor[T]
//   }

//   def selfCheckerForCaseClass[T : Type]: Expr[SelfChecker[T]] = {
//     val repr = TypeRepr.of[T]
//     val caseClass = repr.classSymbol.filter(_.caseFields.length > 0)
//       .getOrElse(report.errorAndAbort("Type is not of case class"))
//     val failures = caseClass.caseFields.map { fieldSymbol =>
//       val name = fieldSymbol.name
//       val fieldRepr = fieldSymbol.tree match {
//         case valDef: ValDef => valDef.tpt.tpe 
//         case defDef: DefDef => defDef.returnTpt.tpe 
//       }
//       fieldRepr.asType match {
//         case '[f] => {
//           Implicits.search(TypeRepr.of[SelfChecker].appliedTo(fieldRepr)) match {
//             case success: ImplicitSearchSuccess => '{
//               (obj: T) =>
//                 ${success.tree.asExpr.asExprOf[SelfChecker[f]]}
//                   .check(${Select('{obj}.asTerm, fieldSymbol).asExpr.asExprOf[f]}) match {
//                     case Right(ok) => List.empty[FailedCheck]
//                     case Left(reasons) =>
//                       reasons.map(_.reportedInto(${Expr(name)}))
//                   }
//             }
//             case failure: ImplicitSearchFailure =>
//               report.errorAndAbort(failure.explanation)
//           }
//         }
//       }
//     }




//     '{
//       new SelfChecker[T] {
//         def check(self: T): Checked[T] = {
//           val fails = ${Expr.ofList(failures)}.map(_.apply(self)).flatten
//           val level = SelfChecker.primitive[T].check(self)
//           Checker.combine(Either.cond(fails.isEmpty, self, fails), level)
//         }
//       }
//     }
//   }

//   /**
//    * Checks types on the same level as T
//    * Example: in (T & A & B), it checks if T is A & B
//    * given that T is the concrete type
//   **/
//   def leveledSelfCheckerFor[T : Type]: Expr[SelfChecker[T]] = {
//     val wholeRepr = TypeRepr.of[T]
//     val concrRepr = findConcreteTypes(wholeRepr)
//       .headOption
//       .getOrElse(report.errorAndAbort(
//         s"Could not find concrete type of type ${wholeRepr.show}"
//       ))

//     (wholeRepr.asType, concrRepr.asType) match {
//       case ('[wholeType], '[concrType]) =>
//         '{
//           new SelfChecker[wholeType] {
//             def check(obj: wholeType): Checked[wholeType] = {
//               val tpeName = ${Expr(wholeRepr.widen.show)}
//               val conName = ${Expr(concrRepr.show)}
//               import checkers.checkAs
//               (s"Self checking ${obj} as if from ${conName} to ${tpeName}")
//               obj.asInstanceOf[concrType].checkAs[wholeType]
//               // checkers.macros.ConfinementAccumulator
//               //   .accumulate[concrType, wholeType]
//               //   .confine(obj.asInstanceOf[concrType])
//             }
//           }.asInstanceOf[SelfChecker[T]]
//         }
//       case _ => ???
//     }
//   }

//   def findConcreteTypes(repr: TypeRepr): List[TypeRepr] = {
//     try {
//       repr.dealias match { 
//         case ref: Refinement => List.empty
//         case ref: TermRef => findConcreteTypes(ref.widen)
//         case and: AndType => findConcreteTypes(and.left) ++ findConcreteTypes(and.right)
//         case app: AppliedType =>
//           findConcreteTypes(app.tycon).map(_.appliedTo(app.args))
//         case ref: TypeRef => {
//           val tpe = ref.translucentSuperType.dealias
//           if(tpe.classSymbol.isDefined && tpe.classSymbol != TypeRepr.of[Any].classSymbol) {
//             List(ref)
//           } else {
//             List.empty
//           }
//         }
//         case _ =>
//           List(repr)
//       }
//     } catch case e: Throwable => List.empty
//   }
// }


