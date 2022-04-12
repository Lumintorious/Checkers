package checkers

final class ValidatingInspace()
inline given [From, To](using VI: ValidatingInspace, C: Checker[From, To]): Conversion[From, Checked[To]] =
  f => C.confine(f)


