package ru.tinkoff.traveladmin.schema.operation

import ru.tinkoff.traveladmin.schema.operation.OperationDSL._
import shapeless.{HList, Witness}

object syntax {
  abstract class Maker[x, T[_, _]] {
    def make[s <: Symbol]: T[s, x]
  }

  abstract class Maker2[x, p, T[_, _, _]] {
    def make[s <: Symbol]: T[s, x, p]
  }

  class MkComplex[x, T[_, _]](maker: Maker[x, T]) {
    def apply[s <: Symbol](witness: Witness.Lt[s]) = maker.make[s]
  }

  class MkComplex2[x, p, T[_, _, _]](maker: Maker2[x, p, T]) {
    def apply[s <: Symbol](witness: Witness.Lt[s]) = maker.make[s]
  }

  implicit class ConsOps[left <: OperationDSL](left: => left) {
    def +>[right](right: => right): left +> right = new +>
  }

  implicit class JoinOps[left <: OperationDSL](left: => left) {
    def <+>[right](right: => right): left <+> right = new <+>
  }

  def action[s <: Symbol](witness: Witness.Lt[s]) = new Action[s]

  def authAction[s <: Symbol](witness: Witness.Lt[s]) = new AuthAction[s]

  def primitive[x] =
    new MkComplex(new Maker[x, Primitive] {
      override def make[s <: Symbol]: Primitive[s, x] =
        new Primitive[s, x]
    })

  def json[x] =
    new MkComplex(new Maker[x, Json] {
      override def make[s <: Symbol]: Json[s, x] =
        new Json[s, x]
    })

  def complex[x] =
    new MkComplex(new Maker[x, Complex] {
      override def make[s <: Symbol]: Complex[s, x] =
        new Complex[s, x]
    })

  def oneOf[x, p] =
    new MkComplex2(new Maker2[x, p, OneOf] {
      override def make[s <: Symbol]: OneOf[s, x, p] = new OneOf
    })

  def file[x] =
    new MkComplex(new Maker[x, File] {
      override def make[s <: Symbol]: File[s, x] = new File
    })

  def hidden[x] =
    new MkComplex(new Maker[x, Hidden] {
      override def make[s <: Symbol]: Hidden[s, x] = new Hidden[s, x]
    })

  def dateTime[x] =
    new MkComplex(new Maker[x, DateTime] {
      override def make[s <: Symbol]: DateTime[s, x] = new DateTime[s, x]
    })

  def date[x] =
    new MkComplex(new Maker[x, Date] {
      override def make[s <: Symbol]: Date[s, x] = new Date[s, x]
    })

  def checkList[x] =
    new MkComplex(new Maker[x, CheckList] {
      override def make[s <: Symbol]: CheckList[s, x] = new CheckList[s, x]
    })

  def dropList[x] =
    new MkComplex(new Maker[x, DropList] {
      override def make[s <: Symbol]: DropList[s, x] = new DropList[s, x]
    })

  def view[x] = new View[x]

  def download[x] = new Download[x]
}
