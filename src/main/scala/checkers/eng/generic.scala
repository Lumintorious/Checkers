package checkers
package eng

inline given [N]: IfBroken[SizeOf[N]] = n => s"'${n}' is not of exactly size ${valueOf[N]}"


