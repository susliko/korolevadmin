package ru.tinkoff

import monocle.macros.Lenses
import ru.tinkoff.traveladmin.access.{AdminOperation, Role}

@Lenses
case class AppState(logged: Boolean = false,
                    roles: List[Role] = Nil,
                    results: Map[String, String] = Map.empty,
                    downloadProgress: Map[String, (Long, Long)] = Map.empty,
                    showingLoader: Map[String, Boolean] = Map.empty,
                    listRenderers: Map[String, List[Object]] = Map.empty,
                    coproductChoices: Map[String, String] = Map.empty) {
  def containsOp(operation: AdminOperation): Boolean =
    roles.flatMap(_.operations).contains(operation)

  def addRenderer(id: String, renderer: Object): AppState = {
    val renderers = renderer :: listRenderers.getOrElse(id, Nil)
    copy(listRenderers = listRenderers.updated(id, renderers))
  }

  def dropRenderer(id: String): AppState = {
    val updaters = listRenderers.getOrElse(id, Nil).dropRight(1)
    copy(listRenderers = listRenderers.updated(id, updaters))
  }

  def setChoice(id: String, newChoice: String): AppState =
    copy(coproductChoices = coproductChoices.updated(id, newChoice))

  def updateDownloadProgress(id: String, progress: (Long, Long)): AppState =
    copy(downloadProgress = downloadProgress.updated(id, progress))

  def deleteDownloadProgress(id: String): AppState =
    copy(downloadProgress = downloadProgress - id)

}
