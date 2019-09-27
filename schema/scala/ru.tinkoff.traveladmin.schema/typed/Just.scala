package ru.tinkoff.traveladmin.schema.typed

import cats.Functor
import cats.syntax.functor._
import ru.tinkoff.tschema.typeDSL.Complete

final class Just[A] private (val value: A) extends AnyVal

object Just {
  def $$$[A]: Complete[Just[A]] = new Complete

  def apply[F[_]: Functor, A](value: F[A]): F[Just[A]] = value.map(new Just(_))
}
