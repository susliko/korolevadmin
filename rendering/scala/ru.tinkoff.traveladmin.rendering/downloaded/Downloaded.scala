package ru.tinkoff.traveladmin.rendering.downloaded

import ru.tinkoff.AppState
import ru.tinkoff.traveladmin.operations.{OperationDefinition, OperationResult}
import ru.tinkoff.traveladmin.schema.operation.OperationDSL.<+>
import ru.tinkoff.traveladmin.{Context, DocumentNode}
import shapeless.HList
import tofu.MonadThrow

trait Downloaded[x, F[_]] {
  def render(state: AppState): List[DocumentNode[F]]
}

object Downloaded {
  def render[x, F[_]](x: x, state: AppState)(implicit renderDef: Downloaded[x, F]) =
    renderDef.render(state)

  implicit def forSingle[x, F[_]: MonadThrow, Name, Params <: HList, Result](
      implicit opDef: OperationDefinition.Aux[x,
                                       Name,
                                       Params,
                                       Result,
                                       OperationResult.Download,
                                       F],
      context: Context[F]): Downloaded[x, F] =
    (state: AppState) => DownloadedRenderer().render(state)

  implicit def forJoin[left, right, F[_]](
                                           implicit leftMaker: Downloaded[left, F],
                                           rightMaker: Downloaded[right, F]
  ): Downloaded[left <+> right, F] =
    (state: AppState) => leftMaker.render(state) ::: rightMaker.render(state)
}
