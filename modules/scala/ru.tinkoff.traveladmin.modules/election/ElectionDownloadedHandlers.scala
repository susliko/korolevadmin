package ru.tinkoff.traveladmin.modules.election

import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.effect.concurrent.Ref
import ru.tinkoff.traveladmin.data.downloadable.CSV
import ru.tinkoff.traveladmin.domain.Vote
import tofu.MonadThrow

trait ElectionDownloadedHandlers[F[_]] {
  def downloadVotes: F[CSV]
}

case class ElectionDownloadedImpl[F[_]: MonadThrow](
    ref: Ref[F, Map[String, Vote]])
    extends ElectionDownloadedHandlers[F] {
  val voteHeader = List("nickname", "birthDate", "votedFor")

  override def downloadVotes: F[CSV] =
    for {
      votes <- ref.get
      voteList = votes.values.toList.map(v =>
        List(v.nickname, v.birthDate, v.voted.entryName))
    } yield CSV(voteHeader :: voteList, "voteProtocol.csv")
}
