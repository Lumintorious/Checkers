package checkers

opaque type SizeOf[N] = Any
inline given [N <: Int](using V: ValueOf[N]): Checker[String, SizeOf[N]] =
  Checker[String, SizeOf[N]](
    _.size == V.value
  )
inline given [L <: Iterable[?], N <: Int](using V: ValueOf[N]): Checker[L, SizeOf[N]] =
  Checker[L, SizeOf[N]](
    _.size == V.value
  )

opaque type SizeGT[N] = Any
inline given [L <: Iterable[?], N <: Int](using V: ValueOf[N]): Checker[L, SizeGT[N]] =
  Checker[L, SizeGT[N]](
    _.size >= V.value
  )

opaque type NonEmpty = Any
inline given [L <: Iterable[?]]: Checker[L, NonEmpty] =
  Checker[L, NonEmpty](
    _.size > 0
  )

opaque type Contains[Item] = Any
inline given mapContains[Item, L <: Map[Item, ?]](using V: ValueOf[Item]): Checker[L, Contains[Item]] =
  Checker[L, Contains[Item]](
    _.contains(V.value)
  )
inline given seqContains[Item, L <: Seq[Item]](using V: ValueOf[Item]): Checker[L, Contains[Item]] =
  Checker[L, Contains[Item]](
    _.contains(V.value)
  )

opaque type DoesntContain[Item] = Any
inline given doesntContain[Item, L <: Seq[Item]](using V: ValueOf[Item]): Checker[L, DoesntContain[Item]] =
  Checker[L, DoesntContain[Item]](
    !_.contains(V.value)
  )
