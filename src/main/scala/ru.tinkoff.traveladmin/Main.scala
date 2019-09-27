package ru.tinkoff.traveladmin

import akka.http.scaladsl.server.Route
import cats.effect.ExitCode
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.typesafe.config.ConfigFactory
import korolev._
import korolev.akkahttp._
import korolev.catsEffectSupport.implicits._
import korolev.server._
import korolev.state.javaSerialization._
import monix.eval.{Task, TaskApp}
import ru.tinkoff.traveladmin.core.{AppComponent, AppModules}
import ru.tinkoff.traveladmin.modules.election.ElectionModule
import ru.tinkoff.traveladmin.schema.typed.Routed.taskRouted
import ru.tinkoff.traveladmin.schema.typed.routableIn._
import ru.tinkoff.AppState

object Main extends TaskApp {

  val component = AppComponent(ConfigFactory.load())
  import component._

  def mkKorolevRoute(modules: AppModules[Task]): Route = {
    import korolevContext._
    import symbolDsl._

    val mkService = akkaHttpService {
      KorolevServiceConfig[Task, AppState, Any](
        head = List(
          'title ("KorolevAdmin"),
          'link (
            'href /= "/static/main.css",
            'rel /= "stylesheet",
            'type /= "text/css"
          ),
          'link (
            'href /= "/static/loader.css",
            'rel /= "stylesheet",
            'type /= "text/css"
          ),
          'meta ('name /= "viewport",
                 'content /= "width=device-width, initial-scale=1.0")
        ),
        router = Router.empty,
        stateStorage = StateStorage.default(AppState()),
        render = {
          case state =>
            'body (
              'main (
                modules.election.render(state)
              )
            )
        }
      )
    }
    mkService(AkkaHttpServerConfig())
  }

  def initApp: Task[(AppModules[Task], Application, Route)] =
    for {
      electionModule <- ElectionModule.make[Task]
      application = Application(component)
    } yield {
      val modules = core.AppModules(electionModule)
      val korolevRoute =
        mkKorolevRoute(modules)
      (modules, application, korolevRoute)
    }

  override def run(args: List[String]): Task[ExitCode] =
    for {
      (modules, application, korolevRoute) <- initApp
      _ <- application.runServer(korolevRoute, modules.election)
    } yield ExitCode.Success
}
