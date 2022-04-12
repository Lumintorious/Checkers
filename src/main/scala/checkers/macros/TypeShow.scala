package checkers.macros

import scala.quoted.*

final case class TypeShow[T](show: String)

inline def showType[T](using TS: TypeShow[T]) = TS.show

object TypeShow {
  inline given [T]: TypeShow[T] = TypeShow[T](getFor[T])

  inline def getFor[T]: String = ${ getForImpl[T] }

  def getForImpl[T : Type](using quotes: Quotes): Expr[String] = {
    import quotes.reflect.*

    val repr = TypeRepr.of[T]
    Expr(getForRepr(repr))
  }

  def getForRepr(using quotes: Quotes)(repr: quotes.reflect.TypeRepr): String = {
    import quotes.reflect.*
    repr match {
      case and: AndType =>
        getForRepr(and.left) + " & " + getForRepr(and.right)
      case ConstantType(const) =>
        const.value.toString
      case AppliedType(base, params) =>
        base.typeSymbol.name + params.map(getForRepr).mkString("[", ", ", "]")
      case TypeRef(repr, name) => name
      case repr => repr.show
    }
  }

  inline def showParamsNames[F](inline fn: => F): List[String] =
    ${ showParamsNamesMacro[F]('fn) }

  def showParamsNamesMacro[F : Type](fn: Expr[F])(using quotes: Quotes): Expr[List[String]] = {
    import quotes.reflect.*

    Expr(showParamsNamesMacro(fn.asTerm))
  }

  def showParamsNamesMacro(using quotes: Quotes)(fn: quotes.reflect.Statement): List[String] = {
    import quotes.reflect.*

    fn match {
      case i: Inlined => return showParamsNamesMacro(i.body)
      case b: Block => return showParamsNamesMacro(b.statements.head)
      case i: Ident => return List.range(0, 22).map("arg" + _)
      case d: DefDef => 
        val all = d.termParamss.flatMap(_.params).collect { case valDef: ValDef => valDef }
        return all.map(_.name)
      case _ =>
    }
    report.errorAndAbort(fn.toString)
  }
}
