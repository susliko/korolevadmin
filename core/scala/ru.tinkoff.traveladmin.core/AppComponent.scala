package ru.tinkoff.traveladmin.core

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import com.typesafe.config.Config
import korolev.{Context => KorolevContext}
import korolev.catsEffectSupport.implicits._
import korolev.state.javaSerialization._
import monix.eval.Task
import monix.execution.Scheduler
import ru.tinkoff.AppState
import ru.tinkoff.traveladmin.Context

case class AppComponent(config: Config) {
  implicit val actorSystem: ActorSystem = ActorSystem("trykorolev", config)
  implicit val materializer: Materializer = ActorMaterializer()
  implicit val appScheduler: Scheduler = Scheduler(actorSystem.dispatcher)
  implicit val korolevContext: Context[Task] = KorolevContext[Task, AppState, Any]
}

