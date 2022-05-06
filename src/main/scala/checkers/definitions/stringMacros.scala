package checkers
package definitions

import quoted.*

object stringMacros {
  transparent inline def guaranteeMatches[
    S <: String & Singleton,
    R <: String
  ]: Guarantee[S, Matches[R]] =
    ${ guaranteeMatchesMacro[S, R] }

  def guaranteeMatchesMacro[
    S <: String : Type,
    R <: String : Type
  ](using quotes: Quotes): Expr[Guarantee[S, Matches[R]]] = {
    import quotes.reflect.*

    val s = collectionMacros.exprFromTypeMacro[S].asExprOf[String].value.get
    val r = collectionMacros.exprFromTypeMacro[R].asExprOf[String].value.get
    println(s"Checking if ${s} matches ${r}")
    if(s.matches(r)) {
      println(s"Checking if ${s} matches ${r} => true")
      '{ new Guarantee[S, Matches[R]](_.asInstanceOf[S & Matches[R]]) }
    } else {
      report.errorAndAbort("Right hand side does not match expected type")
    }
    // (TypeRepr.of[S], TypeRepr.of[R]) match {
    //   case (ConstantType(StringConstant(s)), ConstantType(StringConstant(r))) =>
    //     if(s.matches(r)) {
    //       '{ new Guarantee[S, Matches[R]](_.asInstanceOf[S & Matches[R]]) }
    //     } else {
    //       report.errorAndAbort("Right hand side does not match expected type")
    //     }
    //   case _ =>
    //     report.errorAndAbort("Values not visible at compiletime")
    // }
  }

  transparent inline def guaranteeSize[
    S <: String,
    L <: Int
  ]: Guarantee[S, SizeOf[L]] =
    ${ guaranteeSizeMacro[S, L] }

  def guaranteeSizeMacro[
    S <: String : Type,
    L <: Int : Type
  ](using quotes: Quotes): Expr[Guarantee[S, SizeOf[L]]] = {
    import quotes.reflect.*

    (TypeRepr.of[S], TypeRepr.of[L]) match {
      case (ConstantType(StringConstant(s)), ConstantType(IntConstant(l))) =>
        if(s.size == l) {
          '{ new Guarantee[S, SizeOf[L]](_.asInstanceOf[S & SizeOf[L]]) }
        } else {
          report.errorAndAbort("Right hand side does not match expected type")
        }
      case _ =>
        report.errorAndAbort("Values not visible at compiletime")
    }
  }
    
  //   (s.value zip r.value) match {
  //     case Some(sV, rV) => {
  //       if(sV.matches(rV)) {
  //         '{ new Guarantee[S, Matches[R]](_.asInstanceOf[S & Matches[R]]) }
  //       } else {
  //         report.warning("Here")
  //         report.errorAndAbort("Right hand side does not match expected type")
  //       }
  //     }
  //     case None => {
  //       report.warning("Here4")
  //       report.errorAndAbort("Values not visible at compiletime")
  //     }
  //   }
  // }
}
