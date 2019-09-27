package ru.tinkoff.traveladmin.data.downloadable

import akka.http.scaladsl.model.ContentTypes
import org.manatki.derevo.circeDerivation.{decoder, encoder}
import org.manatki.derevo.derive
import ru.tinkoff.traveladmin.data.downloadable.Downloadable.marshaller

@derive(encoder, decoder, marshaller(ContentTypes.`text/plain(UTF-8)`))
case class JSON(stringContent: String, filename: String) extends Downloadable
