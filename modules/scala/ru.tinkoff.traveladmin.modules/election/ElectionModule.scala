package ru.tinkoff.traveladmin.modules.election

import akka.http.scaladsl.server.Route
import cats.effect.Sync
import cats.effect.concurrent.Ref
import cats.syntax.functor._
import cats.syntax.flatMap._
import ru.tinkoff.AppState
import ru.tinkoff.traveladmin.data.downloadable.CSV
import ru.tinkoff.traveladmin.domain.{Candidate, ElectionSummary, Vote}
import ru.tinkoff.traveladmin.{Context, DocumentNode, StreamCompiler}
import ru.tinkoff.traveladmin.modules.AdminModule
import ru.tinkoff.traveladmin.rendering.downloaded.Downloaded
import ru.tinkoff.traveladmin.rendering.viewed.Viewed
import ru.tinkoff.traveladmin.schema.operation.syntax._
import ru.tinkoff.traveladmin.schema.typed.{HttpComplete, Routed}
import ru.tinkoff.traveladmin.schema.typed.routableIn._
import ru.tinkoff.traveladmin.schema.operation.tsInterop._
import ru.tinkoff.tschema.akkaHttp.MkRoute
import tofu.MonadThrow

case class ElectionModule[
    F[_]: MonadThrow: StreamCompiler: HttpComplete: Routed](
    viewedHandlers: ElectionViewedHandlers[F],
    downloadedHandlers: ElectionDownloadedHandlers[F])(
    implicit context: Context[F])
    extends AdminModule[F] {
  import ElectionModule._

  def name: String = "election"

  def render(state: AppState): List[DocumentNode[F]] =
    Viewed.render(viewedOperations, viewedHandlers, state) :::
      Downloaded.render(downloadedOperations, state)

  def downloadRoute: Option[Route] =
    Some(MkRoute(downloadedOperations.asTypedSchema)(downloadedHandlers))
}

object ElectionModule {

  def make[F[_]: Sync: MonadThrow: StreamCompiler: HttpComplete: Routed](
      implicit context: Context[F]): F[ElectionModule[F]] =
    for {
      ref <- Ref.of[F, Map[String, Vote]](Map.empty)
      viewed = ElectionViewedHandlers.make(ref)
      downloaded = ElectionDownloadedImpl(ref)
    } yield ElectionModule(viewed, downloaded)

  def viewedOperations =
    candidateInfo <+> vote <+> viewResults <+> falsifyResults

  def downloadedOperations = downloadVotes

  def candidateInfo =
    action('candidateInfo) +>
      dropList[Candidate]('candidate) +>
      view[String]

  def vote =
    action('vote) +>
      primitive[String]('nickname) +>
      date[String]('birthDate) +>
      dropList[Candidate]('choice) +>
      view[String]

  def viewResults =
    action('viewResults) +>
      view[ElectionSummary]

  def downloadVotes =
    action('downloadVotes) +>
      download[CSV]

  def falsifyResults =
    action('falsifyResults) +>
      complex[ElectionSummary]('results) +>
      view[String]
}
