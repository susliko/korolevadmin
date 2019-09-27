package ru.tinkoff.traveladmin.schema.typed

import akka.http.scaladsl.marshalling.ToResponseMarshaller
import akka.http.scaladsl.server.{RequestContext, Route, RouteResult}
import cats.FlatMap
import cats.syntax.flatMap._
import monix.eval.Task
import monix.execution.Scheduler
import ru.tinkoff.tschema.akkaHttp.RoutableIn
import shapeless.HList

import scala.concurrent.Future

trait HttpComplete[F[_]] {
  def simple[R: ToResponseMarshaller](result: R,
                                      ctx: RequestContext): F[RouteResult]
}

object HttpComplete {
  implicit val taskComplete: HttpComplete[Task] =
    new HttpComplete[Task] {
      override def simple[R: ToResponseMarshaller](
          result: R,
          ctx: RequestContext): Task[RouteResult] =
        Task.deferFuture(ctx.complete(result))
    }
}

trait Completable[F[_], A] {
  def complete(result: F[A], ctx: RequestContext): F[RouteResult]
}

object Completable {
  final implicit def simpleComplete[F[_]: FlatMap, R: ToResponseMarshaller](
      implicit complete: HttpComplete[F]): Completable[F, R] =
    (fr, ctx) => fr flatMap (complete.simple(_, ctx))
}

trait Routed[F[_]] {
  def route(frr: F[RouteResult]): Future[RouteResult]
}

object Routed {
  implicit def taskRouted(implicit scheduler: Scheduler): Routed[Task] =
    (frr: Task[RouteResult]) => frr.runToFuture
}

object routableIn {

  implicit def routable[F[_], In <: HList, A](
      implicit
      completable: Completable[F, A],
      routed: Routed[F]): RoutableIn[In, F[A], Just[A]] =
    new RoutableIn[In, F[A], Just[A]] {
      override def route(in: In, res: => F[A]): Route =
        ctx => routed.route(completable.complete(res, ctx))
    }

}
