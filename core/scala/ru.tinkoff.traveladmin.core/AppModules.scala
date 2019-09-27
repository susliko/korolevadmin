package ru.tinkoff.traveladmin.core

import tofu.MonadThrow
import ru.tinkoff.traveladmin.modules.election.ElectionModule

case class AppModules[F[_]: MonadThrow](
                                         election: ElectionModule[F]
)
