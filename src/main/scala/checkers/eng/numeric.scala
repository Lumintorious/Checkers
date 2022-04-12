package checkers
package eng

inline given [N]: IfBroken[GT[N]] = n => s"'${n}' wasn't greater than ${valueOf[N]}"
inline given [N]: IfBroken[LT[N]] = n => s"'${n}' wasn't less than ${valueOf[N]}"
inline given [N]: IfBroken[GTEQ[N]] = n => s"'${n}' wasn't greater than or equal to ${valueOf[N]}"
inline given [N]: IfBroken[LTEQ[N]] = n => s"'${n}' wasn't less than or equal to ${valueOf[N]}"
inline given [L, H]: IfBroken[Between[L, H]] = n =>
  s"'${n}' wasn't between ${valueOf[L]} and ${valueOf[H]}"

inline given IfBroken[NonZero] = n => s"'${n}' was zero"
inline given IfBroken[IsZero] = n => s"'${n}' was not zero"

inline given [N]: IfBroken[SizeOf[N]] = l => s"'${l}' wasn't of exactly size ${valueOf[N]}"
inline given IfBroken[AlphaNumeric] = s => s"'${s}' was not completely alphanumeric"