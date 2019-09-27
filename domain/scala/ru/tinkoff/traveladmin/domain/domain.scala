package ru.tinkoff.traveladmin.domain

import cats.syntax.either._
import enumeratum.{Enum, EnumEntry}
import io.circe.syntax._
import io.circe.{Decoder, DecodingFailure, Encoder, HCursor, Json}
import org.manatki.derevo.circeDerivation.{decoder, encoder}
import org.manatki.derevo.derive
import ru.tinkoff.traveladmin.utils.NamedEnumEntry

sealed abstract class Candidate(val name: String)
    extends EnumEntry
    with NamedEnumEntry

object Candidate extends Enum[Candidate] {
  val values = findValues

  case object Hedgehog extends Candidate("Ёжик в тумане")

  case object Winnie extends Candidate("Винни Пух")

  case object Matroskin extends Candidate("Кот Матроскин")

  case object Karlson extends Candidate("Карлсон")

  case object Vovka extends Candidate("Вовка И Так Сойдёт")

  implicit val circeEncoder: Encoder[Candidate] = (a: Candidate) =>
    a.name.asJson
  implicit val circeDecoder: Decoder[Candidate] =
    (c: HCursor) =>
      implicitly[Decoder[String]]
        .apply(c)
        .flatMap(s =>
          s match {
            case "Ёжик в тумане"      => Hedgehog.asRight
            case "Винни Пух"          => Winnie.asRight
            case "Кот Матроскин"      => Matroskin.asRight
            case "Карлсон"            => Karlson.asRight
            case "Вовка И Так Сойдёт" => Vovka.asRight
            case _ =>
              DecodingFailure(s"could not parse $s as Candidate", Nil).asLeft
        })
}

@derive(encoder, decoder)
case class Vote(nickname: String, birthDate: String, voted: Candidate)

@derive(encoder, decoder)
case class CandidateVotes(candidate: Candidate, votesTotal: Int)

@derive(encoder, decoder)
case class ElectionSummary(votesTotal: Int,
                           votesSummary: List[CandidateVotes],
                           winner: Candidate)
