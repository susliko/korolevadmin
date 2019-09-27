package ru.tinkoff.traveladmin.operations

import cats.Applicative
import enumeratum.{Enum, EnumEntry}
import korolev.state.DeviceId
import ru.tinkoff.AppState
import ru.tinkoff.traveladmin.{Access, Context, DocumentNode}
import ru.tinkoff.traveladmin.access.AdminOperation
import ru.tinkoff.traveladmin.schema.operation.OperationDSL
import ru.tinkoff.traveladmin.schema.operation.OperationDSL._
import ru.tinkoff.traveladmin.utils.{FileToJson, NamedEnumEntry}
import shapeless.ops.hlist.Prepend
import shapeless.{::, HList, HNil, Witness}
import tofu.MonadThrow

trait OperationDefinition[tree, F[_]] {
  type Params <: HList

  type Result

  type Name

  type ResultFormat

  val operation: Option[AdminOperation] = None

  val operationName: Option[String] = None

  val paramInits: List[Context[F] => ParamRenderer[F]] = Nil
}

object OperationDefinition extends ParamsOperationDef with ResultsOperationDef {
  type Aux[x, name, params <: HList, result, resFormat, F[_]] =
    OperationDefinition[x, F] {
      type Params = params
      type Result = result
      type Name = name
      type ResFormat = resFormat
    }

  implicit def forAction[name <: Symbol, F[_]](implicit w: Witness.Aux[name])
    : Aux[Action[name], name, HNil, Unit, Unit, F] =
    new OperationDefinition[Action[name], F] {
      type Params = HNil
      type Result = Unit
      type Name = w.T
      type ResFormat = Unit
      override val operation: Option[AdminOperation] = None
      override val operationName: Option[String] = Some(w.value.name)
    }

  implicit def forAuthAction[name <: Symbol, F[_]](
      implicit w: Witness.Aux[name])
    : Aux[AuthAction[name], name, DeviceId :: HNil, Unit, Unit, F] =
    new OperationDefinition[AuthAction[name], F] {
      type Params = DeviceId :: HNil
      type Result = Unit
      type Name = w.T
      type ResFormat = Unit
      override val operation: Option[AdminOperation] = None
      override val operationName: Option[String] = Some(w.value.name)
    }

  implicit def forCons[start,
                       end,
                       SP <: HList,
                       EP <: HList,
                       SR,
                       ER,
                       SN,
                       EN,
                       SRF,
                       ERF,
                       F[_]](
      implicit start: OperationDefinition.Aux[start, SN, SP, SR, SRF, F],
      end: OperationDefinition.Aux[end, EN, EP, ER, ERF, F],
      prepend: Prepend[SP, EP])
    : Aux[start +> end, SN, prepend.Out, ER, ERF, F] =
    new OperationDefinition[start +> end, F] {
      type Params = prepend.Out
      type Result = end.Result
      type Name = start.Name
      type ResFormat = end.ResFormat

      override val operationName: Option[String] = start.operationName
      override val operation: Option[AdminOperation] = start.operation
      override val paramInits = start.paramInits ::: end.paramInits
    }
}

trait ParamsOperationDef {
  import OperationDefinition.Aux

  def mkForParam[name <: Symbol, T, F[_], param[_, _] <: OperationDSL](
      initParam: Context[F] => ParamRenderer[F])(implicit w: Witness.Aux[name])
    : Aux[param[name, T], Unit, T :: HNil, Unit, Unit, F] =
    new OperationDefinition[param[name, T], F] {
      override val paramInits = List(initParam)
      type Params = T :: HNil
      type Result = Unit
      type Name = Unit
      type ResFormat = Unit
    }

  implicit def forCheckList[name <: Symbol,
                            T <: EnumEntry with NamedEnumEntry: Enum,
                            F[_]: Applicative](
      implicit witness: Witness.Aux[name])
    : Aux[CheckList[name, List[T]], Unit, List[T] :: HNil, Unit, Unit, F] =
    mkForParam[name, List[T], F, CheckList](context =>
      CheckListParamRenderer(witness.value.name, context))

  implicit def forDropList[name <: Symbol,
                           T <: EnumEntry with NamedEnumEntry: Enum,
                           F[_]: Applicative](
      implicit witness: Witness.Aux[name])
    : Aux[DropList[name, T], Unit, T :: HNil, Unit, Unit, F] =
    mkForParam[name, T, F, DropList](context =>
      DropListParamRenderer(witness.value.name, context))

  implicit def forDateTime[name <: Symbol, T, F[_]](
      implicit witness: Witness.Aux[name])
    : Aux[DateTime[name, T], Unit, T :: HNil, Unit, Unit, F] =
    mkForParam[name, T, F, DateTime](context =>
      DateTimeParamRenderer(witness.value.name, context))

  implicit def forDate[name <: Symbol, T, F[_]](
      implicit witness: Witness.Aux[name])
    : Aux[Date[name, T], Unit, T :: HNil, Unit, Unit, F] =
    mkForParam[name, T, F, Date](context =>
      DateParamRenderer(witness.value.name, context))

  implicit def forHiddenParam[name <: Symbol, T, F[_]: Applicative](
      implicit witness: Witness.Aux[name])
    : Aux[Hidden[name, T], Unit, T :: HNil, Unit, Unit, F] =
    mkForParam[name, T, F, Hidden](context =>
      HiddenParamRenderer(witness.value.name, context))

  implicit def forPrimitiveParam[name <: Symbol, T, F[_]: Applicative](
      implicit witness: Witness.Aux[name])
    : Aux[Primitive[name, T], Unit, T :: HNil, Unit, Unit, F] =
    mkForParam[name, T, F, Primitive](context =>
      PrimitiveParamRenderer(witness.value.name, context))

  implicit def forJsonParam[name <: Symbol, T, F[_]: Applicative](
      implicit witness: Witness.Aux[name])
    : Aux[Json[name, T], Unit, T :: HNil, Unit, Unit, F] =
    mkForParam[name, T, F, Json](context =>
      JsonParamRenderer(witness.value.name, context))

  implicit def forComplexParam[name <: Symbol, T, F[_]: MonadThrow](
      implicit witness: Witness.Aux[name],
      renderer: ComplexRenderer[F, T],
  ): Aux[Complex[name, T], Unit, T :: HNil, Unit, Unit, F] =
    mkForParam[name, T, F, Complex](context =>
      ComplexParamRenderer(witness.value.name, context, renderer))

  implicit def forOneOf[name <: Symbol, Base, T, F[_]](
      implicit witness: Witness.Aux[name],
      renderer: ComplexRenderer[F, Base :: T :: HNil]
  ): Aux[OneOf[name, Base, T], Unit, Base :: HNil, Unit, Unit, F] =
    new OperationDefinition[OneOf[name, Base, T], F] {
      val initParam: Context[F] => ParamRenderer[F] = context =>
        OneOfParamRenderer(witness.value.name, context, renderer)
      override val paramInits = List(initParam)
      type Params = Base :: HNil
      type Result = Unit
      type Name = Unit
      type ResFormat = Unit
    }

  implicit def forFile[name <: Symbol, T, F[_]: MonadThrow](
      implicit witness: Witness.Aux[name],
      fileTojson: FileToJson[F, T]
  ): Aux[File[name, T], Unit, T :: HNil, Unit, Unit, F] =
    mkForParam[name, T, F, File](context =>
      FileParamRenderer(witness.value.name, context, fileTojson))

}

trait ResultsOperationDef {
  import OperationDefinition.Aux

  implicit def forViewResult[T, F[_]]
    : Aux[View[T], Unit, HNil, T, OperationResult.View, F] =
    new OperationDefinition[View[T], F] {
      type Params = HNil
      type Result = T
      type Name = Unit
      type ResFormat = OperationResult.View
    }

  implicit def forDownloadResult[T, F[_]]
    : Aux[Download[T], Unit, HNil, T, OperationResult.Download, F] =
    new OperationDefinition[Download[T], F] {
      type Params = HNil
      type Result = T
      type Name = Unit
      type ResFormat = OperationResult.Download
    }
}
