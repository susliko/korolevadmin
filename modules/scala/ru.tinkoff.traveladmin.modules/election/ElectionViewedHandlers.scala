package ru.tinkoff.traveladmin.modules.election

import cats.Monad
import cats.effect.concurrent.Ref
import ru.tinkoff.traveladmin.domain.{
  Candidate,
  CandidateVotes,
  ElectionSummary,
  Vote
}
import ru.tinkoff.traveladmin.utils.tuples._
import cats.syntax.applicative._
import cats.syntax.flatMap._
import cats.syntax.functor._
import tofu.MonadThrow

case class ElectionViewedHandlers[F[_]](
    candidateInfo: Tuple1[Candidate] => F[String],
    vote: ((String, String, Candidate)) => F[String],
    viewResults: Unit => F[ElectionSummary],
    falsifyResults: Tuple1[ElectionSummary] => F[String]
)

object ElectionViewedHandlers {
  def make[F[_]: MonadThrow](
      ref: Ref[F, Map[String, Vote]]): ElectionViewedHandlers[F] = {
    val handlers = Handlers(ref)
    ElectionViewedHandlers((handlers.candidateInfo _).tupled,
                           (handlers.vote _).tupled,
                           Unit => handlers.viewResults,
                           (handlers.falsifyResults _).tupled)
  }
}

case class Handlers[F[_]: MonadThrow](ref: Ref[F, Map[String, Vote]]) {
  def candidateInfo(candidate: Candidate): F[String] = {
    val text = candidate match {
      case Candidate.Hedgehog  => "Носит пожитки в узелке"
      case Candidate.Winnie    => "Любит пчёл"
      case Candidate.Matroskin => "Имеет корову"
      case Candidate.Karlson   => "Живёт на крыше"
      case Candidate.Vovka     => "Хорошо топит печь"
    }
    Monad[F].pure(text)
  }

  def vote(nickname: String, birthDate: String, choice: Candidate): F[String] =
    for {
      votes <- ref.get
      alreadyVoted = votes.contains(nickname)
      emptyName = nickname.isEmpty
      emptyBirthDate = birthDate.isEmpty
      _ <- ref
        .update(m => m.updated(nickname, Vote(nickname, birthDate, choice)))
        .whenA(!alreadyVoted && !emptyName && !emptyBirthDate)
    } yield {
      if (emptyName) "Укажите никнейм"
      else if (alreadyVoted) s"Голос $nickname уже зарегистрирован"
      else if (emptyBirthDate) "Укажите дату рождения"
      else "Ваш голос учтён!"
    }

  def viewResults: F[ElectionSummary] =
    for {
      votes <- ref.get
      votesSummary = votes.values
        .groupBy(v => v.voted)
        .map(p => CandidateVotes(p._1, p._2.size))
        .toList
      winner = if (votesSummary.nonEmpty)
        votesSummary.maxBy(_.votesTotal).candidate
      else Candidate.Hedgehog
    } yield ElectionSummary(votes.size, votesSummary, winner)

  def falsifyResults(results: ElectionSummary): F[String] =
    Monad[F].pure("Так не пойдёт. Наши выборы честные!")
}
