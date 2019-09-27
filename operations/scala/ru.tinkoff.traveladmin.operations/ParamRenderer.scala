package ru.tinkoff.traveladmin.operations

import cats.Applicative
import cats.instances.list._
import cats.syntax.functor._
import cats.syntax.traverse._
import cats.syntax.flatMap._
import enumeratum.{Enum, EnumEntry}
import io.circe.syntax._
import ru.tinkoff.traveladmin.{Access, Context, DocumentNode}
import shapeless.{::, HNil}
import fs2.Stream
import fs2.text
import ru.tinkoff.AppState
import ru.tinkoff.traveladmin.utils.{FileToJson, NamedEnumEntry}
import tofu.MonadThrow

trait ParamRenderer[F[_]] {
  val name: String

  def render(state: AppState, opName: String): DocumentNode[F]

  def value(access: Access[F], opName: String): F[String]
}

case class PrimitiveParamRenderer[F[_]](name: String, context: Context[F])
    extends ParamRenderer[F] {
  val elementId = context.elementId()

  def render(state: AppState, opName: String): DocumentNode[F] = {
    import context.symbolDsl._
    'section (
      'label ('div (name), 'input (elementId, 'type /= "text", 'name /= name)))
  }

  def value(access: context.Access, opName: String) = access.valueOf(elementId)
}

case class JsonParamRenderer[F[_]](name: String, context: Context[F])
    extends ParamRenderer[F] {
  val elementId = context.elementId()

  def render(state: AppState, opName: String): DocumentNode[F] = {
    import context.symbolDsl._
    'section (
      'label ('div (name), 'pre ('code (elementId, 'name /= name)))
    )
  }

  def value(access: context.Access, opName: String) = access.valueOf(elementId)
}

case class HiddenParamRenderer[F[_]](name: String, context: Context[F])
    extends ParamRenderer[F] {
  val elementId = context.elementId()

  override def render(state: AppState, opName: String): DocumentNode[F] = {
    import context.symbolDsl._
    'section (
      'label ('div (name),
              'input (elementId, 'type /= "password", 'name /= name)))
  }

  def value(access: context.Access, opName: String) = access.valueOf(elementId)
}

case class CheckListParamRenderer[F[_]: Applicative, x <: EnumEntry with NamedEnumEntry](
    name: String,
    context: Context[F])(implicit val enum: Enum[x])
    extends ParamRenderer[F] {

  val checkboxes = enum.values.map(_.name) zip
    List.fill(enum.values.length)(context.elementId())

  def render(state: AppState, opName: String): DocumentNode[F] = {
    import context.symbolDsl._
    'div (
      'div (name),
      checkboxes.map(
        p =>
          'section (
            'label ('input (p._2,
                            'class /= "input",
                            'type /= "checkbox",
                            'name /= name,
                            'value /= p._1),
                    p._1)))
    )
  }

  override def value(access: context.Access, opName: String): F[String] = {
    checkboxes
      .map(
        c =>
          access
            .property(c._2)
            .get('checked)
            .map(s => (c._1, s == "true")))
      .toList
      .sequence
      .map(_.filter(_._2).map(_._1).asJson.toString)
  }
}

case class DropListParamRenderer[F[_]: Applicative, x <: EnumEntry with NamedEnumEntry](
    name: String,
    context: Context[F])(implicit val enum: Enum[x])
    extends ParamRenderer[F] {

  val elementId = context.elementId()

  override def render(state: AppState, opName: String): DocumentNode[F] = {
    import context.symbolDsl._
    'section (
      'label (
        'div (name),
        'select (
          elementId,
          enum.values.map(_.name).map(n => 'option ('value /= n, n))
        )
      ))
  }

  override def value(access: context.Access, opName: String): F[String] =
    for {
      value <- access.valueOf(elementId)
    } yield value
}

case class DateTimeParamRenderer[F[_]](name: String, context: Context[F])
    extends ParamRenderer[F] {
  val elementId = context.elementId()

  def render(state: AppState, opName: String): DocumentNode[F] = {
    import context.symbolDsl._
    'section (
      'label ('div (name),
              'input (elementId,
                      'class /= "input",
                      'type /= "datetime-local",
                      'name /= name)))
  }

  def value(access: context.Access, opName: String) = access.valueOf(elementId)
}

case class DateParamRenderer[F[_]](name: String, context: Context[F])
    extends ParamRenderer[F] {
  val elementId = context.elementId()

  def render(state: AppState, opName: String): DocumentNode[F] = {
    import context.symbolDsl._
    'section (
      'label ('div (name),
              'input (elementId,
                      'class /= "input",
                      'type /= "date",
                      'name /= name)))
  }

  def value(access: context.Access, opName: String) = access.valueOf(elementId)
}

case class ComplexParamRenderer[F[_], T](name: String,
                                         context: Context[F],
                                         renderer: ComplexRenderer[F, T])
    extends ParamRenderer[F] {

  import context.symbolDsl._

  override def render(state: AppState, opName: String): DocumentNode[F] =
    'section (
      'label (
        'div (name),
        renderer.render(state, s"$opName.$name")
      )
    )

  override def value(access: Access[F], opName: String): F[String] =
    renderer.value(access, s"$opName.$name")
}

case class OneOfParamRenderer[F[_], Base, T](
    name: String,
    context: Context[F],
    renderer: ComplexRenderer[F, Base :: T :: HNil])
    extends ParamRenderer[F] {

  import context.symbolDsl._

  override def render(state: AppState, opName: String): DocumentNode[F] =
    'section (
      'label (
        'div (name),
        renderer.render(state, s"$opName.$name")
      )
    )

  override def value(access: Access[F], opName: String): F[String] =
    renderer.value(access, s"$opName.$name")
}

case class FileParamRenderer[F[_]: MonadThrow, T](name: String,
                                                  context: Context[F],
                                                  fileToJson: FileToJson[F, T])
    extends ParamRenderer[F] {
  val elementId = context.elementId()

  import context.symbolDsl._

  override def render(state: AppState, opName: String): DocumentNode[F] =
    'section (
      'label (
        'div (name),
        'input (elementId, 'type /= "file"),
        state.downloadProgress
          .get(s"$opName.$name")
          .map { progress =>
            'div (s"${progress._1} / ${progress._2} bytes")
          }
          .getOrElse(levsha.Document.Empty)
      ))

  override def value(access: Access[F], opName: String): F[String] =
    for {
      files <- access.downloadFilesAsStream(elementId)
      id = s"$opName.$name"
      fileStream = files.headOption.map(
        file =>
          (file.name,
           Stream
             .repeatEval[F, Option[Array[Byte]]](for {
               state <- access.state
               progress = state.downloadProgress.getOrElse(id, (0L, 0L))
               bytes <- file.data.pull()
               size = file.data.size.getOrElse(progress._2)
               _ <- bytes.fold(Applicative[F].unit)(
                 b =>
                   access.transition(
                     _.updateDownloadProgress(id,
                                              (progress._1 + b.length, size))))
             } yield bytes)
             .unNoneTerminate
             .flatMap(bytes => Stream(bytes: _*))
             .through(text.utf8Decode)))
      fileString <- fileStream
        .map(s => fileToJson.convert(s._1, s._2))
        .getOrElse(Applicative[F].pure(""))
    } yield fileString
}
