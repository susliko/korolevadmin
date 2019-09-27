package ru.tinkoff.traveladmin.access

import java.util.UUID

import io.circe.generic.extras.{Configuration, ConfiguredJsonCodec}
import korolev.state.DeviceId
import org.manatki.derevo.circeDerivation.{decoder, encoder}
import org.manatki.derevo.derive

@ConfiguredJsonCodec
sealed trait AdminStored

object AdminStored {
  implicit val customConfig: Configuration = Configuration.default
    .withDiscriminator("type")
    .withSnakeCaseConstructorNames
}

@derive(encoder, decoder)
case class User(id: UUID = UUID.randomUUID(),
                username: String,
                pswdHash: String,
                roles: List[Role],
                deviceId: Option[DeviceId] = None)
    extends AdminStored
