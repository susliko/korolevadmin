package ru.tinkoff.traveladmin.rendering.viewed

import io.circe.Encoder
import cats.instances.list._
import cats.syntax.traverse._
import cats.syntax.functor._
import cats.syntax.either._
import cats.syntax.applicativeError._
import cats.syntax.flatMap._
import ru.tinkoff.traveladmin.operations.{OperationDefinition, OperationResult}
import ru.tinkoff.traveladmin.utils.HListDecoder
import ru.tinkoff.traveladmin.utils.HListDecoder.syntax._
import io.circe.syntax._
import levsha.Document
import ru.tinkoff.AppState
import ru.tinkoff.traveladmin.{Context, DocumentNode}
import shapeless.HList
import shapeless.ops.hlist.Tupler
import tofu.MonadThrow

case class ViewedRenderer[x,
                          name,
                          Params <: HList,
                          PTuple,
                          Result,
                          F[_]: MonadThrow](handler: PTuple => F[Result])(
    implicit
    opDef: OperationDefinition.Aux[x,
                                   name,
                                   Params,
                                   Result,
                                   OperationResult.View,
                                   F],
    paramsTupler: Tupler.Aux[Params, PTuple],
    context: Context[F],
    decoder: HListDecoder[Params],
    encoder: Encoder[Result]) {
  import context._
  import symbolDsl._

  val name = opDef.operationName.getOrElse("UNKNOWN")


  def render(state: AppState): List[DocumentNode[F]] = {

    def setResult(access: Access, text: String): F[Unit] =
      access.transition(AppState.results.modify(m => m.updated(name, text)))

    val params = opDef.paramInits.map(f => f(context))
    val renderResult: List[DocumentNode[F]] = {
      val resultContent = state.results.getOrElse(name, "")
      val waiting = state.showingLoader.getOrElse(name, false)
      val loader: DocumentNode[F] =
        if (waiting) 'div ('class /= "lds-dual-ring") else Document.Empty
      List(
        'section (
          'class /= "submit-section",
          'button ('class /= "accent-button", 'type /= "submit", "submit"),
          loader),
        'section (
          'label ('class /= "label",
                  'div ('class /= "label-col", "result"),
                  'pre ('code (resultContent.filter(_ != '\"'))))
        ),
        event('submit) { access =>
          for {
            params <- params
              .map(_.value(access, name))
              .sequence
            deviceId <- access.sessionId.map(_.deviceId)
            _ <- access.transition(
              AppState.showingLoader.modify(m => m.updated(name, true)))
            decoded = (deviceId :: params)
              .as[Params]
              .leftFlatMap(_ => params.as[Params])
            _ <- decoded match {
              case Left(error) =>
                setResult(access, error.message)
              case Right(decParams) =>
                handler(paramsTupler(decParams))
                  .map(_.asJson.spaces2)
                  .flatMap(s => setResult(access, s))
                  .handleErrorWith(e => setResult(access, e.getMessage))
            }
            _ <- access.transition(
              AppState.showingLoader.modify(m => m.updated(name, false)))
          } yield ()
        }
      )
    }

    List(
      'details ('summary (s"$name"),
                'div ('class /= "details-content",
                      'form ('id /= s"$name",
                             params.map(_.render(state, name)),
                             renderResult)))
    )
  }
}
