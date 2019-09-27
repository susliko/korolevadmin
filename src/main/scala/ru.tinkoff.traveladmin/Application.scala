package ru.tinkoff.traveladmin

import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.RouteConcatenation._
import cats.syntax.functor._
import korolev.akkahttp.{AkkaHttpServerConfig, AkkaHttpService}
import monix.eval.Task
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import ru.tinkoff.traveladmin.core.AppComponent
import ru.tinkoff.traveladmin.modules.AdminModule

case class Application(component: AppComponent) {
  import component._

  def runServer(korolevRoute: Route,
                modules: AdminModule[Task]*): Task[Unit] = {
    val downloadRoutes = modules.toList.flatMap(_.downloadRoute)

    val staticRoutes = korolevRoute

    val route =
      downloadRoutes.foldLeft(staticRoutes)((acc, route) => route ~ acc)
    for {
      _ <- Task.deferFuture(Http().bindAndHandle(route, "0.0.0.0", 8080)).void
    } yield ()
  }
}
