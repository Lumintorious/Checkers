package checkers
package definitions

object string {
  inline given matches[
    S <: String & Singleton,
    R <: String
  ]: Guarantee[S, Matches[R]] = {
    stringMacros.guaranteeMatches[S, R]
  }
  
  transparent inline given sizeOf[
    S <: String & Singleton,
    L <: Int & Singleton
  ]: Guarantee[S, SizeOf[L]] = {
    collectionMacros.sizeOfGuarantee[S, L]
  }
  
  transparent inline given sizeLT[
    S <: String & Singleton,
    L <: Int & Singleton
  ]: Guarantee[S, SizeLT[L]] = {
    collectionMacros.sizeLTGuarantee[S, L]
  }
  
  transparent inline given sizeGT[
    S <: String & Singleton,
    L <: Int & Singleton
  ]: Guarantee[S, SizeGT[L]] = {
    collectionMacros.sizeGTGuarantee[S, L]
  }
}