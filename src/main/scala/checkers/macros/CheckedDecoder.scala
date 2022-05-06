package checkers.macros

import io.circe.Decoder

trait CheckedDecoder[T] {
  val hasConcrete: HasConcrete[T]
  val unsafeConcreteDecoder: Decoder[hasConcrete.ConcreteType]
}
