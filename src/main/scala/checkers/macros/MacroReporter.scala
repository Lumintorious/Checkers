package checkers.macros

import quoted.*

class MacroReporter(using val quotes: Quotes) {
  import quotes.reflect.*

  def checkerNotFound[ForType : Type](explanation: String): Nothing = {
    report.errorAndAbort(s"Could not find a ${TypeRepr.of[ForType].show}. ${explanation}")
  }

  def introspectorNotFound[ForType : Type](explanation: String): Nothing = {
    report.errorAndAbort(s"Could not find a ${TypeRepr.of[ForType].show}. ${explanation}")
  }

  def singletonNotFound[ForType : Type](explanation: String): Nothing = {
    report.errorAndAbort(
      s"Could not find singleton and value of type ${TypeRepr.of[ForType].show}." +
        explanation
    )
  }

  def noConcreteType[InType : Type]: Nothing = {
    report.errorAndAbort(
      s"Type ${TypeRepr.of[InType].show} has no concrete type base. " +
        s"It is an opaque constraint, a refinement type or an intersection of those. " +
        s"A specific class backing it up is required."
    )
  }

  def notProduct[T : Type]: Nothing = {
    report.errorAndAbort(
      s"Type ${TypeRepr.of[T].show} is not a product type. "
    )
  }
}
