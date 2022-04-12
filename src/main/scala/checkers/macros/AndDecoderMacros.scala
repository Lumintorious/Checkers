// package checkers
// package macros

// import quoted.*
// import io.circe.Decoder
// import io.circe.HCursor
// import io.circe.DecodingFailure
// import io.circe.CursorOp.DownField

// object AndDecoderMacros {
  
// }

// class AndDecoderMacros(val quotes: Quotes) {
//   import quotes.reflect.*
//   given Quotes = quotes

//   def getDoubleDecoder[A : Type, B : Type](
//     decoderAExpr: Expr[Decoder[A]],
//     decoderBExpr: Expr[Decoder[B]]
//   ): Expr[Decoder[?]] = {
//     val sh = quotes.reflect.TypeReprMethods.show
//     val tpeA = TypeRepr.of[A]
//     val tpeB = TypeRepr.of[B]
//     if(tpeA <:< tpeB) {
//       decoderAExpr
//     } else if (tpeB <:< tpeA) {
//       decoderBExpr
//     } else {
//       report.errorAndAbort(s"Conflicting types, both with Decoders: ${sh(tpeA)}, ${sh(tpeB)}")
//     }
//   }

//   def getCheckedDecoder[A : Type, B : Type](
//     decoderExpr: Expr[Decoder[A]],
//     checkerExpr: Expr[Checker[A, B]]
//   ): Expr[Decoder[A & B]] = {
//     '{
//       val decoder = ${decoderExpr}
//       given Checker[A, B] = ${checkerExpr}
//       new Decoder[A & B] {
//         def apply(c: HCursor): Decoder.Result[A & B] = {
//           decoder.apply(c) match {
//             case Right(valueA) =>
//               val aAndB = valueA.checkAs[B]
//               aAndB match {
//                 case Right(ab) =>
//                   Right(ab)
//                 case Left(errs) =>
//                   Left(DecodingFailure(errs.map(failed =>
//                     c.history match {
//                       case DownField(field) :: tail => failed.reportedInto(field)
//                       case _ => failed
//                     } 
//                   ).toList.mkString("; "), c.history))
//               }
//             case Left(fail) => Left(fail)
//           }
//         }
//       }
//     }
//   }
// }
