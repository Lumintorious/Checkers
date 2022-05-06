package checkers
package definitions

object collection {
  transparent inline given guaranteeSizeOfIterable[
    S <: (Seq[?] | Map[?, ?]),
    L <: Int
  ]: Guarantee[S, SizeOf[L]] = {
    collectionMacros.sizeOfGuarantee[S, L]
  }
  
  transparent inline given guaranteeSizeLTIterable[
    S <: (Seq[?] | Map[?, ?]),
    L <: Int & Singleton
  ]: Guarantee[S, SizeLT[L]] = {
    collectionMacros.sizeLTGuarantee[S, L]
  }
  
  transparent inline given guaranteeSizeGTIterable[
    S <: (Seq[?] | Map[?, ?]),
    L <: Int & Singleton
  ]: Guarantee[S, SizeGT[L]] = {
    collectionMacros.sizeGTGuarantee[S, L]
  }
}
