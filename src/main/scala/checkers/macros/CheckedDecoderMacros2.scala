// package checkers.macros

// import quoted.*
// import io.circe.*
// import checkers.SelfChecker

// object CheckedDecoderMacros2 {
//   inline def makeBasic[T]: Decoder[T] = ${ makeBasicMacro[T] }

//   def makeBasicMacro[T : Type](using Quotes): Expr[Decoder[T]] =
//     CheckedDecoderMacros2().make[T]
// }

// class CheckedDecoderMacros2(using val quotes: Quotes) {
//   import quotes.reflect.*

//   def make[T : Type]: Expr[Decoder[T]] =
//     val repr = TypeRepr.of[T]
//     val concreteRepr = findConcreteTypes(repr).head
//     val owner = Symbol.spliceOwner.name

//     concreteRepr.asType match {
//       case '[c] =>
//         (Implicits.search(TypeRepr.of[Decoder].appliedTo(concreteRepr)),
//         Implicits.search(TypeRepr.of[SelfChecker].appliedTo(repr))) match {
//           case (successD: ImplicitSearchSuccess, successC: ImplicitSearchSuccess) =>
//             '{
//               new Decoder[T] {
//                 def apply(c: HCursor): Decoder.Result[T] =
//                   val concreteDecoder = ${successD.tree.asExpr.asExprOf[Decoder[c]]}
//                   concreteDecoder.apply(c) match {
//                     case Right(actual) =>
//                       if(c.history.isEmpty) {
//                         ${successC.tree.asExpr.asExprOf[SelfChecker[T]]}.check(actual.asInstanceOf[T]) match {
//                           case Right(t) => Right(t)
//                           case Left(l) =>
//                             Left(DecodingFailure(l.toList.mkString("; "), c.history))
//                         }
//                       } else {
//                         Right(actual.asInstanceOf[T])
//                       }
//                     case Left(err) => Left(err)
//                   }
                  
                  
//               }
//             }
//           case (failure: ImplicitSearchFailure, _) =>
//             report.errorAndAbort(
//               failure.explanation
//             ) 
//           case (_, b: ImplicitSearchFailure) => 
//             // report.warning(a.explanation)
//             report.errorAndAbort(b.explanation)
//             // report.errorAndAbort("Could not find SelfChecker")
//         }
//       }

// }
