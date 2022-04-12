// package checkers.macros

// import scala.quoted.*
// import checkers.*
// import scala.reflect.TypeTest
// import scala.annotation.meta.field

// object ConfinementAccumulator {

//   inline def accumulate[Actual, Target]: Checker[Actual, Target] =
//     ${ accumulateImpl[Actual, Target] }

//   def accumulateImpl[Actual : Type, Target : Type](using quotes: Quotes): Expr[Checker[Actual, Target]] =
//     val x = ConfinementAccumulator()
//     import x.quotes.reflect.*
//     try {
//       '{
//         ${x.accumulateFor(
//           TypeRepr.of[Actual],
//           TypeRepr.of[Target]
//         ).asExpr}.asInstanceOf[Checker[Actual, Target]]
//       }
//     } catch {
//       case e: Throwable =>
//         e.printStackTrace()
//         throw e

//     }

//   inline def spliceOwnerName: String = ${spliceOwnerNameImpl()}

//   def spliceOwnerNameImpl()(using quotes: Quotes): Expr[String] =
//     import quotes.reflect.*

//     Expr(Symbol.spliceOwner.owner.owner.name)
// }


// class ConfinementAccumulator(using val quotes: Quotes) {
//   import quotes.reflect.*

//   private type T = TypeRepr

//   def nameOf(repr: T): String = {
    
//     repr match {
//       case ConstantType(const) =>
//         const.value.toString
//       case AppliedType(base, params) =>
//         base.typeSymbol.name + params.map(nameOf).mkString("[", ", ", "]")
//       case TypeRef(repr, name) => name
//       case repr => repr.show
//     }
//   }

//   def reportConfinementNotFound(in: T, out: T) = {
//     val e = Exception()
//     report.warning(e.getStackTrace().toList.take(5).mkString("\n"))
//     report.errorAndAbort(
//       s""" 
//         Cannot confine ${nameOf(in)}
//         to ${nameOf(out)}. 
//         Missing Checker. Also tried using a SelfChecked, failed too.
//       """
//     )
//   }

//   def lastResortCallSelfChecker(in: T, out: T): Term = {
//     val and = AndType(in, out)
//     Implicits.search(TypeRepr.of[SelfChecker].appliedTo(out)) match {
//       case success: ImplicitSearchSuccess =>
//         (in.asType, out.asType) match {
//           case ('[inT], '[outT]) => {
//             '{
//               new Checker[inT, outT] {
//                 def confine(self: inT): Checked[inT & outT] = {
//                   (s"Using self checker of ${${Expr(out.show)}}")
//                   (s"Failed to find Checker, using SelfChecker for ${${Expr(out.show)}}")
//                   ${success.tree.asExprOf[SelfChecker[outT]]}
//                     .check(self.asInstanceOf[outT]).asInstanceOf[Checked[inT & outT]]
//                 }
//               }
//             }.asTerm
//           }
//         }
//       case failure: ImplicitSearchFailure =>
//         reportConfinementNotFound(in, out)
//     }
//   }

//   def confinementForImpl(in: T, out: T): Option[Term] =
//     Implicits.search(TypeRepr.of[Checker].appliedTo(List(in, out))) match {
//       case success: ImplicitSearchSuccess => Some(success.tree)
//       case failure: ImplicitSearchFailure => None
//     }

//   def confinementFor(in: T, out: T): Option[Term] = {
//     confinementForImpl(in, out).orElse {
//       in match {
//         case AndType(left, right) =>
//           confinementFor(left, out)
//             .orElse(confinementFor(right, out))
//         case _ => None
//       }
//     }
//   }

//   def typeShowFor(out: T): Option[Term] =
//     Implicits.search(TypeRepr.of[TypeShow].appliedTo(out)) match {
//       case success: ImplicitSearchSuccess => Some(success.tree)
//       case failure: ImplicitSearchFailure => None
//     }

//   def messageFor(out: T): Option[Term] =
//     Implicits.search(TypeRepr.of[BrokenConfinementMessage].appliedTo(out)) match {
//       case success: ImplicitSearchSuccess => Some(success.tree)
//       case failure: ImplicitSearchFailure => None
//     }

//   def attemptToGiveMessage(in: T, out: T, checkers: Term): Term =
//     (in.asType, out.asType, messageFor(out)) match {
//       case ('[inT], '[outT], Some(message)) => '{
//         val prev: Checker[inT, outT] = ${checkers.asExpr.asExprOf[Checker[inT, outT]]}
//         new Checker[inT, outT] {
//           def confine(actual: inT): Checked[inT & outT] = {
//             prev.confine(actual).swap.map(l => List(l.map(
//               broken => 
//                 if(broken.isBasic) {
//                   broken.copy(explanation = 
//                     ${message.asExpr.asExprOf[BrokenConfinementMessage[outT]]}.format(actual),
//                     isBasic = false
//                   )
//                 } else {
//                   broken
//                 }
//               )
//             ).head).swap
//           }
//         }
//       }.asTerm
//       case ('[inT], '[outT], _) => '{
//         val prev: Checker[inT, outT] = ${checkers.asExpr}.asInstanceOf[Checker[inT, outT]]
//         new Checker[inT, outT] {
//           def confine(actual: inT): Checked[inT & outT] =
//             prev.confine(actual).swap.map(l => List(l.map(
//             broken =>
//               if(broken.isBasic) {
//                 broken.copy(explanation = 
//                   s"'${actual}' is not ${${Expr(TypeShow.getForRepr(out.asInstanceOf))}}",
//                   isBasic = false
//                 )
//               } else {
//                 broken
//               }
//             )
//             ).head).swap
//         }
//       }.asTerm
//       case _ => checkers
//     }

//   def accumulateSimpleType(in: T, out: T): Term = {
//     val result: Term = confinementFor(in, out) match {
//       case Some(term) => {
//         term
//       }
//       case None => {
//         (in.asType, out.asType, out) match {
//           case (_, _, out: AppliedType) => {
//             lastResortCallSelfChecker(in, out)
//             // reportConfinementNotFound(in, out)
//           }
//           case ('[inT], '[outT], out) if out.classSymbol.isDefined && !(out =:= TypeRepr.of[Nothing]) => '{
//             new Checker[inT, outT] {
//               def confine(actual: inT): Checked[inT & outT] =
//                 val name = ${Expr(out.show)}
//                 Either.cond(
//                   actual.isInstanceOf[outT],
//                   actual.asInstanceOf[inT & outT],
//                   List(FailedCheck.explain(s"'${actual}' was not an instance of ${name}")
//                     .copy(isBasic = true))
//                 )
//             }
//           }.asTerm
//           case _ => {
//             lastResortCallSelfChecker(in, out)
//             // reportConfinementNotFound(in, out)
//           }
//         }
//       }
//     }
//     attemptToGiveMessage(in, out, result)
//   }

//   def namesAndTypesOfRefinement(ref: TypeRepr): Map[String, TypeRepr] = {
//     ref match {
//       case ref: Refinement =>
//         namesAndTypesOfRefinement(ref.parent) +((ref.name, ref.info))
//       case _ => Map.empty
//     }
//   }

//   def accumulateRefinementType(in: T, out: Refinement): Term = {
//     val tSymbol = in.typeSymbol
//     val fields = namesAndTypesOfRefinement(out)
//     (in.asType, out.asType) match {
//       case ('[inT], '[outT]) =>
//       val fs = fields.toList.map { (name, repr) =>
//         if(tSymbol.declaredField(name) == Symbol.noSymbol) {
//           report.errorAndAbort(s"Type doesn't have field ${name} specified in refinement")
//         } else {
//           val refinementFieldType = repr
//           val inputFieldType =
//             tSymbol.declaredField(name)
//               .tree
//               .asInstanceOf[ValDef]
//               .tpt.tpe
          
//           (inputFieldType.asType, refinementFieldType.asType) match {
//             case ('[fType],'[outR]) => {
//               '{
//                 (t: inT) => {
//                   ${Select('{t}.asTerm, tSymbol.declaredField(name)).asExprOf[fType]}
//                     .checkAs[outR].map(_ => t).reportedAs(${Expr(name)})
//                 }
//               }.asExprOf[inT => Checked[?]]
//             }
//           }
//         }
//       }
//       val fsExpr = Expr.ofList(fs)
//         '{
//           new Checker[inT, outT] {
//             def confine(self: inT): Checked[inT & outT] = {
//               val default: Checked[inT & outT] = Right(self.asInstanceOf[inT & outT])
//               ${fsExpr}.foldLeft(default) { (acc, current) =>
//                 Checker.combine(acc, current(self.asInstanceOf[inT]).asInstanceOf[Checked[?]])
//               }
//             }
//           }
//         }.asTerm
//     }
//   }
    

//   def accumulateOpaqueTypeRef(in: T, out: TypeRef): Term =
//     // confinementFor(in, out).getOrElse(reportConfinementNotFound(in, out))
//     accumulateSimpleType(in, out)

//   def accumulateTypeRef(in: T, out: TypeRef): Term =
//     val previous = accumulateOpaqueTypeRef(in, out)
//       // if(out == out.dealias) {
//       //   accumulateSimpleType(in, out)
//       // } else {
//       //   accumulateFor(in, out.dealias)
//       // }
    
//     // val result = (in.asType, out.asType, out.dealias.asType) match {
//     //   case ('[inT], '[outT], '[dealiasedOutT]) => '{
//     //     val prevConfinement: Checker[inT, outT] =
//     //       ${previous.asExpr.asExprOf[Checker[inT, outT]]}
//     //     val combined = prevConfinement.widen[outT]
//     //     combined
//     //   }.asTerm
//     // }
//     attemptToGiveMessage(in, out, previous)

//   def accumulateAndType(in: T, out: AndType): Term =
//     val left  = accumulateFor(in, out.left)
//     val right = accumulateFor(in, out.right)

//     (in.asType, out.left.asType, out.right.asType) match {
//       case ('[inT], '[leftT], '[rightT]) => '{
//         val leftConfinement: Checker[inT, leftT] =
//           ${left.asExpr.asExprOf[Checker[inT, leftT]]}
//         val rightConfinement: Checker[inT, rightT] =
//           ${right.asExpr.asExprOf[Checker[inT, rightT]]}
//         val combined = (leftConfinement && rightConfinement)
//         combined
//       }.asTerm
//     }

//   def accumulateOrType(in: T, out: OrType): Term =
//     val left  = accumulateFor(in, out.left)
//     val right = accumulateFor(in, out.right)

//     (in.asType, out.left.asType, out.right.asType) match {
//       case ('[inT], '[leftT], '[rightT]) => '{
//         val leftConfinement: Checker[inT, leftT] =
//           ${left.asExpr.asExprOf[Checker[inT, leftT]]}
//         val rightConfinement: Checker[inT, rightT] =
//           ${right.asExpr.asExprOf[Checker[inT, rightT]]}
//         val combined = (leftConfinement || rightConfinement)
//         combined
//       }.asTerm
//     }

//   def accumulateNotType(in: T, notType: AppliedType): Term =
//     val inner = accumulateFor(in, notType.args.head)

//     (in.asType, notType.args.head.asType) match {
//       case ('[inT], '[notT]) => '{
//         val innerConfinement: Checker[inT, notT] =
//           ${inner.asExpr.asExprOf[Checker[inT, notT]]}
//         innerConfinement.negated
//       }.asTerm
//     }

//   def accumulateFor(in: T, out: T): Term =
//     val result = out match {
//       case ref: Refinement => accumulateRefinementType(in, ref)
//       case ref: TermRef if !ref.isSingleton => accumulateFor(in, ref.widen)
//       case and: AndType => accumulateAndType(in, and)
//       case and: OrType => accumulateOrType(in, and)
//       case not: AppliedType if not.tycon =:= TypeRepr.of[Not] => accumulateNotType(in, not)
//       case ref: TypeRef if ref.isOpaqueAlias && ref.classSymbol.isEmpty => accumulateOpaqueTypeRef(in, ref)
//       case ref: TypeRef => accumulateTypeRef(in, ref)
//       case _ => accumulateSimpleType(in, out)
//     }
//     result
// }
