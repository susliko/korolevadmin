package ru.tinkoff.traveladmin.data.downloadable

import akka.http.scaladsl.model.ContentType
import akka.http.scaladsl.marshalling.{Marshaller, ToResponseMarshaller}
import akka.http.scaladsl.model.HttpEntity.CloseDelimited
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.{
  ContentDispositionTypes,
  `Content-Disposition`
}
import akka.stream.scaladsl.Source
import akka.util.ByteString
import org.manatki.derevo.Derivation
import org.manatki.derevo.circeDerivation.{decoder, encoder}

trait Downloadable {
  def filename: String
  def stringContent: String
}

object Downloadable {

  object marshaller extends Derivation[ToResponseMarshaller] {
    def apply[T <: Downloadable](
        contentType: ContentType): ToResponseMarshaller[T] =
      Marshaller
        .withFixedContentType(contentType) { f: T =>
          HttpResponse(
            headers = List(
              `Content-Disposition`(ContentDispositionTypes.attachment,
                                    Map("filename" -> f.filename))),
            entity = CloseDelimited(contentType,
                                    Source.single(ByteString(f.stringContent)))
          )
        }
  }
}
