package ru.tinkoff.traveladmin.schema.operation

import korolev.state.DeviceId
import ru.tinkoff.traveladmin.schema.operation.OperationDSL._
import ru.tinkoff.traveladmin.schema.typed.Just
import ru.tinkoff.tschema.syntax._
import ru.tinkoff.tschema.typeDSL._
import shapeless._

trait TypedSchemaInterop[x <: OperationDSL, y <: DSLDef] {
  def asTypedSchema: y
}

object tsInterop {

  implicit class InteropOps[x <: OperationDSL](x: x) {
    def asTypedSchema[y <: DSLDef](
        implicit tsInterop: TypedSchemaInterop[x, y]) =
      tsInterop.asTypedSchema
  }

  implicit def forAction[x] =
    new TypedSchemaInterop[Action[x], Prefix[x] :> Key[x] :> Get] {
      def asTypedSchema: Prefix[x] :> Key[x] :> Get =
        new Prefix[x] :> new Key[x] :> new Get
    }

  implicit def forAuthAction[x] =
    new TypedSchemaInterop[
      AuthAction[x],
      Prefix[x] :> Key[x] :> Get :> Cookie[Witness.`'deviceId`.T, DeviceId]] {
      def asTypedSchema
        : Prefix[x] :> Key[x] :> Get :> Cookie[Witness.`'deviceId`.T,
                                               DeviceId] =
        new Prefix[x] :> new Key[x] :> new Get :> new Cookie[
          Witness.`'deviceId`.T,
          DeviceId]
    }

  implicit def forParam[param[_, _] <: OperationParam, name, x] =
    new TypedSchemaInterop[param[name, x], QueryParam[name, x]] {
      def asTypedSchema: QueryParam[name, x] = new QueryParam
    }

  implicit def forResult[result[_] <: OperationResult, x] =
    new TypedSchemaInterop[result[x], Complete[Just[x]]] {
      def asTypedSchema: Complete[Just[x]] = new Complete
    }

  implicit def forCons[left <: OperationDSL,
                       leftInt <: DSLDef,
                       right <: OperationDSL,
                       rightInt <: DSLDef](
      implicit leftInterop: TypedSchemaInterop[left, leftInt],
      rightInterop: TypedSchemaInterop[right, rightInt]) =
    new TypedSchemaInterop[left +> right, leftInt :> rightInt] {
      override def asTypedSchema: leftInt :> rightInt =
        leftInterop.asTypedSchema :> rightInterop.asTypedSchema
    }

  implicit def forJoin[left <: OperationDSL,
                       leftInt <: DSLDef,
                       right <: OperationDSL,
                       rightInt <: DSLDef](
      implicit leftInterop: TypedSchemaInterop[left, leftInt],
      rightInterop: TypedSchemaInterop[right, rightInt]
  ) = new TypedSchemaInterop[left <+> right, leftInt <|> rightInt] {
    override def asTypedSchema: leftInt <|> rightInt =
      leftInterop.asTypedSchema <|> rightInterop.asTypedSchema
  }
}
