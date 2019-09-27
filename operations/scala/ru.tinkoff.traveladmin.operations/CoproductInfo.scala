package ru.tinkoff.traveladmin.operations

import cats.data.NonEmptyList
import shapeless.{:+:, CNil, Typeable}

trait CoproductInfo[F[_], B, T] {
  def names: NonEmptyList[String]
  def renderers: NonEmptyList[ComplexRenderer[F, B]]
}

object CoproductInfo {
  implicit def forSingle[F[_], B, T <: B](
      implicit typ: Typeable[T],
      renderer: ComplexRenderer[F, T]) =
    new CoproductInfo[F, B, T] {
      override def names: NonEmptyList[String] =
        NonEmptyList.of(typ.describe.takeWhile(_ != '['))

      override def renderers: NonEmptyList[ComplexRenderer[F, B]] =
        NonEmptyList.of(renderer.asInstanceOf[ComplexRenderer[F, B]])
    }

  implicit def forMany[F[_], B, L, R](
      implicit left: CoproductInfo[F, B, L],
      right: CoproductInfo[F, B, R]) =
    new CoproductInfo[F, B, L :+: R :+: CNil] {
      override def names: NonEmptyList[String] = left.names ::: right.names

      override def renderers: NonEmptyList[ComplexRenderer[F, B]] =
        left.renderers ::: right.renderers
    }
}
