package checkers

import io.circe.Decoder
import checkers.macros.CheckedDecoderMacros
import scala.util.NotGiven
import io.circe.Encoder
import io.circe.parser.*
import io.circe.DecodingFailure
import compiletime.*
import io.circe.parser.decodeAccumulating
import io.circe.syntax.*
import checkers.macros.HasConcrete
import checkers.macros.*

object IntrospectedCodec {
  class UnsafeDecodingPermission

  type DecodedChecked[T] = Either[io.circe.Error | List[FailedCheck], T]

  inline def decodeAndIntrospect[T](json: String)(using I: Introspector[T]): DecodedChecked[T] = {
    inline given UnsafeDecodingPermission()
    val parsed = io.circe.parser.parse(json)
    val decoder = summonInlineFromConcrete[Decoder, T]

    val unsafeResult = parsed.flatMap(p => decoder.apply(CheckSkipPermissionCursor(p)))
    unsafeResult match {
      case Left(err) => {
        println("Was fail from the start")
        Left(err)
      }
      case Right(result) => {
        result.asInstanceOf[T].selfChecked
      }
    }
  }

  inline given decoder[T](using I: Introspector[T]): Decoder[T] =
    CheckedDecoderMacros.make[T]

  inline given encoder[T](using I: Introspector[T])(using NotGiven[Encoder[T]]): Encoder[T] =
    CheckedDecoderMacros.makeEncoder[T]
}