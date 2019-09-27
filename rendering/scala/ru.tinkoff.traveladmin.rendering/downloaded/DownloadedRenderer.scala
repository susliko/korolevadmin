package ru.tinkoff.traveladmin.rendering.downloaded

import cats.Monad
import ru.tinkoff.traveladmin.operations.{OperationDefinition, OperationResult}
import ru.tinkoff.traveladmin.{Context, DocumentNode}
import shapeless.HList
import cats.syntax.functor._
import cats.syntax.flatMap._
import cats.instances.list._
import cats.syntax.traverse._
import levsha.Document
import ru.tinkoff.AppState

case class DownloadedRenderer[x, name, Params <: HList, Result, F[_]: Monad]()(
    implicit context: Context[F],
    opDef: OperationDefinition.Aux[x,
                                   name,
                                   Params,
                                   Result,
                                   OperationResult.Download,
                                   F]
) {
  import context._
  import symbolDsl._

  val name = opDef.operationName.getOrElse("UNKNOWN")
  val params = opDef.paramInits.map(f => f(context))

  def render(state: AppState): List[DocumentNode[F]] = {
    val waiting = state.showingLoader.getOrElse(name, false)
    val loader: DocumentNode[F] =
      if (waiting) 'div ('class /= "lds-dual-ring") else Document.Empty

    List(
      'details (
        'summary (s"$name"),
        'div (
          'class /= "details-content",
          'form (
            'class /= "form",
            'action /= s"/$name",
            'method /= "GET",
            'id /= s"$name",
            params.map(_.render(state, name)),
            'section (
              'class /= "submit",
              'button ('class /= "accent-button",
                       'type /= "submit",
                       "download"),
              loader
            ),
            event('submit) { access =>
              for {
                paramValues <- params
                  .map(_.value(access, name))
                  .sequence
                paramNames = params.map(_.name)
                _ <- access.transition(
                  AppState.showingLoader.modify(m => m.updated(name, true)))
                values = paramNames
                  .zip(paramValues)
                  .map(p => s"${p._1}=${p._2}")
                  .mkString("&")
                _ <- access.evalJs(s"document.location.href = '$name?$values'")
                _ <- access.transition(
                  AppState.showingLoader.modify(m => m.updated(name, false)))
              } yield ()
            }
          )
        )
      )
    )
  }

}
