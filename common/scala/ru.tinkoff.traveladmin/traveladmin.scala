package ru.tinkoff

import korolev.{Context => KorolevContext}
import korolev.Context.{Access => KorolevAccess}
import levsha.Document

package object traveladmin {
  type DocumentNode[F[_]] = Document.Node[KorolevContext.Effect[F, AppState, Any]]
  type Context[F[_]] = KorolevContext[F, AppState, Any]
  type Access[F[_]] = KorolevAccess[F, AppState, Any]
  type StreamCompiler[F[_]] = fs2.Stream.Compiler[F, F]
}
