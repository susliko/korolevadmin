package ru.tinkoff.traveladmin.data.downloadable

import akka.http.scaladsl.model._
import cats.effect.Sync
import cats.syntax.functor._
import org.manatki.derevo.circeDerivation.{decoder, encoder}
import org.manatki.derevo.derive
import ru.tinkoff.traveladmin.data.downloadable.Downloadable.marshaller
import fs2.Stream
import fs2.text
import io.circe.syntax._
import ru.tinkoff.traveladmin.utils.FileToJson
import tofu.MonadThrow

@derive(encoder, decoder, marshaller(ContentTypes.`text/csv(UTF-8)`))
case class CSV(content: List[List[String]],
               filename: String,
               colDelim: String = ",",
               lineDelim: String = "\n")
    extends Downloadable {
  def stringContent: String =
    content.map(_.mkString(colDelim)).mkString(lineDelim)
}

object CSV {
  implicit def fileToJson[F[_]: MonadThrow](implicit compiler: fs2.Stream.Compiler[F, F]): FileToJson[F, CSV] =
    (filename: String, stream: Stream[F, String]) =>
      stream
        .through(text.lines)
        .compile(compiler)
        .toList
        .map(lines =>
          CSV(lines.map(_.split(",").toList), filename).asJson.noSpaces)
}
