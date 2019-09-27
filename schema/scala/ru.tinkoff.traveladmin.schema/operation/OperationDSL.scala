package ru.tinkoff.traveladmin.schema.operation

import shapeless.HList

trait OperationDSL

trait OperationParam extends OperationDSL

trait OperationResult extends OperationDSL

trait OperationChain extends OperationDSL

object OperationDSL {
  class Action[name] extends OperationDSL
  class AuthAction[name] extends OperationDSL

  class View[x] extends OperationResult
  class Download[x] extends OperationResult

  class Primitive[name, x] extends OperationParam
  class Json[name, x] extends OperationParam
  class Complex[name, x] extends OperationParam
  class OneOf[name, x, p] extends OperationParam
  class File[name, x] extends OperationParam
  class Hidden[name, x] extends OperationParam
  class CheckList[name, x] extends OperationParam
  class DropList[name, x] extends OperationParam
  class DateTime[name, x] extends OperationParam
  class Date[name, x] extends OperationParam

  class +>[left, right] extends OperationChain
  class <+>[left, right] extends OperationChain
}
