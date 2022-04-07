package checkers

import checkers.macros.ConfinementAccumulator

final class ValidatingInspace()
inline given [From, To](using VI: ValidatingInspace, C: Checker[From, To]): Conversion[From, ValidatedConfinement[To]] =
  f => C.confine(f)


