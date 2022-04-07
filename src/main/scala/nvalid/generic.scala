package confinement

import eng.given

opaque type SameAs[X] = Any
inline given [A, B]: Checker[A, SameAs[B]] =
  Checker[A, SameAs[B]] { _ == valueOf[B] } 

opaque type In[L] = Any
inline given [T, L <: Seq[T]](using V: ValueOf[L]): Checker[T, In[L]] =
  Checker[T, In[L]](
    V.value.contains(_)
  )

opaque type NotIn[L] = Any
inline given [T, L <: Seq[T]](using V: ValueOf[L]): Checker[T, NotIn[L]] =
  Checker[T, NotIn[L]](
    !V.value.contains(_)
  )