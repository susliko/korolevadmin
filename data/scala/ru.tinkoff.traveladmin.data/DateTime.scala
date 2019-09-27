package ru.tinkoff.traveladmin.data

import java.time.LocalDateTime

import io.circe.{Decoder, Encoder}
import ru.tinkoff.tschema.param.{HttpParam, Param, ParamSource}

class DateTime(val value: LocalDateTime) extends AnyVal

object DateTime {
  implicit val encoder: Encoder[DateTime] =
    Encoder.encodeLocalDateTime.contramap(_.value)
  implicit val decoder: Decoder[DateTime] =
    Decoder.decodeLocalDateTime.map(d => new DateTime(d))
  implicit val param: Param[ParamSource.All, DateTime] =
    HttpParam.tryParam(s => new DateTime(LocalDateTime.parse(s)))
}
