package ru.tinkoff.traveladmin.rendering.viewed

import io.circe.Encoder
import ru.tinkoff.AppState
import ru.tinkoff.traveladmin.operations.{OperationDefinition, OperationResult}
import ru.tinkoff.traveladmin.schema.operation.OperationDSL._
import ru.tinkoff.traveladmin.utils.HListDecoder
import ru.tinkoff.traveladmin.{Context, DocumentNode}
import shapeless.ops.hlist.Tupler
import shapeless.ops.record.Selector
import shapeless.{HList, HNil, LabelledGeneric}
import tofu.MonadThrow

trait Viewed[x, F[_], VH[*[_]]] {
  def render(handlers: VH[F], state: AppState): List[DocumentNode[F]]
}

object Viewed {
  def apply[x,
            F[_]: MonadThrow,
            Name,
            Params <: HList,
            PTuple,
            Result,
            VH[*[_]],
            Handlers <: HList](x: x, handlers: VH[F], state: AppState)(
      implicit opDef: OperationDefinition.Aux[x,
                                              Name,
                                              Params,
                                              Result,
                                              OperationResult.View,
                                              F],
      tupler: Tupler.Aux[Params, PTuple],
      context: Context[F],
      lg: LabelledGeneric.Aux[VH[F], Handlers],
      selector: Selector.Aux[Handlers, Name, PTuple => F[Result]],
      decoder: HListDecoder[Params],
      encoder: Encoder[Result]): List[DocumentNode[F]] =
    forSingle.render(handlers, state)

  def render[x, F[_], VH[*[_]]](x: x, handlers: VH[F], state: AppState)(
      implicit viewedMaker: Viewed[x, F, VH]): List[DocumentNode[F]] =
    viewedMaker.render(handlers, state)

  implicit def forSingle[x,
                         F[_]: MonadThrow,
                         Name,
                         Params <: HList,
                         PTuple,
                         Result,
                         VH[*[_]],
                         Handlers <: HList](
      implicit opDef: OperationDefinition.Aux[x,
                                              Name,
                                              Params,
                                              Result,
                                              OperationResult.View,
                                              F],
      tupler: Tupler.Aux[Params, PTuple],
      context: Context[F],
      lg: LabelledGeneric.Aux[VH[F], Handlers],
      selector: Selector.Aux[Handlers, Name, PTuple => F[Result]],
      decoder: HListDecoder[Params],
      encoder: Encoder[Result]): Viewed[x, F, VH] =
    (handlers: VH[F], state: AppState) =>
      ViewedRenderer(selector(lg.to(handlers))).render(state)

  implicit def forJoin[left, right, F[_], VH[*[_]]](
                                                     implicit leftMaker: Viewed[left, F, VH],
                                                     rightMaker: Viewed[right, F, VH])
    : Viewed[left <+> right, F, VH] =
    (handlers: VH[F], state: AppState) =>
      leftMaker.render(handlers, state) ::: rightMaker.render(handlers, state)
}
