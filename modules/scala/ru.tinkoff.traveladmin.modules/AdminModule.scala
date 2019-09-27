package ru.tinkoff.traveladmin.modules

import akka.http.scaladsl.server.Route
import ru.tinkoff.AppState
import ru.tinkoff.traveladmin.DocumentNode

trait AdminModule[F[_]] {
  def name: String
  def render(state: AppState): List[DocumentNode[F]]
  def downloadRoute: Option[Route]
}
