package ru.tinkoff.traveladmin.utils

import io.circe.{Decoder, DecodingFailure, Json}
import io.circe.parser._
import shapeless.{::, HList, HNil, Typeable}
import cats.syntax.either._

trait HListDecoder[T <: HList] {
  def decode(s: List[String]): Either[DecodingFailure, T]
}

object HListDecoder {

  object syntax {
    implicit class HlistDecoderOps(val strings: List[String]) extends AnyVal {
      def as[T <: HList](implicit decoder: HListDecoder[T]): Either[DecodingFailure, T] =
        decoder.decode(strings)
    }
  }

  implicit def forHNil: HListDecoder[HNil] = (_: List[String]) => Right(HNil)

  implicit def forHCons[T, R <: HList](
      implicit
      typ: Typeable[T],
      valueDec: Decoder[T],
      listDec: HListDecoder[R]): HListDecoder[T :: R] =
    (strings: List[String]) => {
      import syntax._
      val strOpt = strings.headOption
      val input = Either
        .cond(strOpt.isDefined,
              strOpt.get,
              DecodingFailure(s"not enough input to decode to ${typ.describe}",
                              Nil))

      def tryDecode(j: Json) =
        j.as[T]
          .leftMap(
            f =>
              DecodingFailure(
                s"""${j.toString} can not be parsed as ${typ.describe}""",
                f.history))

      for {
        str <- input.map(_.filter(_ != '\\')) // quotes unescaping
        j1 = parse(str).leftMap(f => DecodingFailure(f.message, Nil))
        j2 = Json.fromString(str)
        r1 = j1.flatMap(tryDecode)
        r2 = tryDecode(j2)
        decL <- r1.leftFlatMap(_ => r2)
        decR <- strings.tail.as[R]
      } yield decL :: decR
    }
}
