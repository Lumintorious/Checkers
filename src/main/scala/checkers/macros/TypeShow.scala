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
      case ConstantType(const) =>
        const.value.toString
      case AppliedType(base, params) =>
        base.typeSymbol.name + params.map(getForRepr).mkString("[", ", ", "]")
      case TypeRef(repr, name) => name
      case repr => repr.show
    }
  }
}
