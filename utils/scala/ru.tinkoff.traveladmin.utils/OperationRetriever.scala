package ru.tinkoff.traveladmin.utils

import shapeless.ops.record.Selector
import shapeless.{HList, LabelledGeneric, Witness}

object OperationRetriever {
  implicit class Ops[C <: Product](val c: C) extends AnyVal {
    def get[name <: Symbol, params <: HList, FuncType](name: Witness)(
        implicit lg: LabelledGeneric.Aux[C, params],
        selector: Selector.Aux[params, name.T, FuncType]): FuncType =
      selector(lg.to(c))
  }
}
