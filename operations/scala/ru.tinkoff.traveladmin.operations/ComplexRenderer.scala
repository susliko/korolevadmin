package ru.tinkoff.traveladmin.operations

import cats.Applicative
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.instances.list._
import cats.syntax.traverse._
import levsha.Document
import ru.tinkoff.AppState
import ru.tinkoff.traveladmin.{Access, Context, DocumentNode}
import shapeless.labelled.FieldType
import shapeless.{::, HList, HNil, LabelledGeneric, Witness}
import tofu.MonadThrow
import enumeratum.{Enum, EnumEntry}
import ru.tinkoff.traveladmin.utils.NamedEnumEntry

trait ComplexRenderer[F[_], T] {
  def render(state: AppState, name: String): List[DocumentNode[F]]

  def value(access: Access[F], name: String): F[String]
}

object ComplexRenderer {

  implicit def genericRenderer[F[_]: MonadThrow, T, L <: HList](
      implicit lg: LabelledGeneric.Aux[T, L],
      renderer: ComplexRenderer[F, L],
      context: Context[F]
  ): ComplexRenderer[F, T] =
    new ComplexRenderer[F, T] {
      import context.symbolDsl._
      override def render(state: AppState,
                          name: String): List[DocumentNode[F]] =
        List('section ('class /= "form-block", renderer.render(state, name)))

      override def value(access: context.Access, name: String): F[String] =
        renderer.value(access, name).map(s => s"{${s.dropRight(1)}}")
    }

  implicit def consRenderer[F[_]: MonadThrow, K <: Symbol, V, T <: HList](
      implicit valueRenderer: ComplexRenderer[F, FieldType[K, V]],
      restRenderer: ComplexRenderer[F, T],
      context: Context[F]
  ): ComplexRenderer[F, FieldType[K, V] :: T] =
    new ComplexRenderer[F, FieldType[K, V] :: T] {
      override def render(state: AppState,
                          name: String): List[DocumentNode[F]] =
        valueRenderer.render(state, name) ::: restRenderer.render(state, name)

      override def value(access: context.Access, name: String): F[String] =
        for {
          value <- valueRenderer.value(access, name)
          rest <- restRenderer.value(access, name)
        } yield s"$value,$rest"
    }

  implicit def hnilRenderer[F[_]: MonadThrow](
      implicit context: Context[F]): ComplexRenderer[F, HNil] =
    new ComplexRenderer[F, HNil] {
      override def render(state: AppState,
                          name: String): List[DocumentNode[F]] =
        Nil

      override def value(access: context.Access, name: String): F[String] =
        Applicative[F].pure("")
    }

  implicit def fieldRenderer[F[_]: MonadThrow, K <: Symbol, V](
      implicit
      w: Witness.Aux[K],
      context: Context[F],
      valueRenderer: ComplexRenderer[F, V],
  ): ComplexRenderer[F, FieldType[K, V]] =
    new ComplexRenderer[F, FieldType[K, V]] {
      import context.symbolDsl._
      override def render(state: AppState,
                          name: String): List[DocumentNode[F]] =
        List(
          'section (
            'label (
              'div (w.value.name),
              valueRenderer.render(state, s"$name.${w.value.name}")
            )
          ))

      override def value(access: Access[F], name: String): F[String] =
        valueRenderer
          .value(access, s"$name.${w.value.name}")
          .map(value => s""" "${w.value.name}":$value""")
    }

  implicit def coproductRenderer[F[_]: MonadThrow, B, T](
      implicit
      context: Context[F],
      info: CoproductInfo[F, B, T]
  ): ComplexRenderer[F, B :: T :: HNil] =
    new ComplexRenderer[F, B :: T :: HNil] {
      import context.symbolDsl._
      import context.event

      val elementId = context.elementId()

      def chosenRenderer(state: AppState, name: String) = {
        val names = info.names.toList
        val choice = state.coproductChoices.getOrElse(name, names.head)
        names
          .zip(info.renderers.toList)
          .find(p => p._1 == choice)
          .getOrElse((names.head, info.renderers.head))
      }

      override def render(state: AppState,
                          name: String): List[DocumentNode[F]] =
        List(
          'section (
            'select (
              elementId,
              info.names.map(n => 'option ('value /= n, n)).toList,
              event('change) { access =>
                for {
                  newChoice <- access.valueOf(elementId)
                  _ <- access.transition(s => s.setChoice(name, newChoice))
                } yield ()
              }
            )),
          chosenRenderer(state, name)._2.render(state, name)
        )

      override def value(access: context.Access, name: String): F[String] =
        for {
          state <- access.state
          (rendName, renderer) = chosenRenderer(state, name)
          value <- renderer.value(access, name)
        } yield s"""{"$rendName":$value}"""
    }

  def numberRenderer[F[_]: MonadThrow, V](step: String)(
      implicit context: Context[F]): ComplexRenderer[F, V] =
    new ComplexRenderer[F, V] {
      import context.symbolDsl._

      val elementId = context.elementId()

      override def render(state: AppState,
                          name: String): List[DocumentNode[F]] =
        List(
          'input (elementId, 'type /= "number", 'step /= step)
        )

      override def value(access: context.Access, name: String): F[String] =
        access.valueOf(elementId).map(v => s""" "$v" """)
    }

  def textRenderer[F[_]: MonadThrow, V](
      implicit context: Context[F]): ComplexRenderer[F, V] =
    new ComplexRenderer[F, V] {

      import context.symbolDsl._

      val elementId = context.elementId()

      override def render(state: AppState,
                          name: String): List[DocumentNode[F]] =
        List(
          'input (elementId, 'type /= "text")
        )

      override def value(access: context.Access, name: String): F[String] =
        access.valueOf(elementId).map(v => s""" "$v" """)
    }

  implicit def intRenderer[F[_]: MonadThrow](
      implicit context: Context[F]): ComplexRenderer[F, Int] =
    numberRenderer("1")

  implicit def longRenderer[F[_]: MonadThrow](
      implicit context: Context[F]): ComplexRenderer[F, Long] =
    numberRenderer("1")

  implicit def doubleRenderer[F[_]: MonadThrow](
      implicit context: Context[F]): ComplexRenderer[F, Double] =
    numberRenderer("0.01")

  implicit def stringRenderer[F[_]: MonadThrow](
      implicit context: Context[F]): ComplexRenderer[F, String] =
    textRenderer

  implicit def booleanRenderer[F[_]: MonadThrow](
      implicit context: Context[F]): ComplexRenderer[F, Boolean] =
    new ComplexRenderer[F, Boolean] {

      import context.symbolDsl._

      val elementId = context.elementId()

      override def render(state: AppState,
                          name: String): List[DocumentNode[F]] =
        List(
          'input (elementId, 'type /= "checkbox")
        )

      override def value(access: context.Access, name: String): F[String] =
        access
          .property(elementId)
          .get('checked)
          .map(s => s" ${(s == "true").toString}")
    }

  implicit def listRenderer[F[_]: MonadThrow, T](
      implicit context: Context[F],
      complexRenderer: ComplexRenderer[F, T]): ComplexRenderer[F, List[T]] =
    new ComplexRenderer[F, List[T]] {
      import context._
      import symbolDsl._

      override def render(state: AppState,
                          name: String): List[DocumentNode[F]] = {
        val renderers = state.listRenderers
          .getOrElse(name, Nil)
          .map(_.asInstanceOf[ComplexRenderer[F, T]])

        val addButton = 'section (
          'button (
            'class /= "normal-button",
            'type /= "submit",
            "add",
            event('click) { access =>
              for {
                _ <- access.transition(
                  state =>
                    state
                      .addRenderer(name, complexRenderer))
              } yield ()
            }
          ))

        val removeButton =
          if (renderers.nonEmpty)
            'button (
              'class /= "normal-button",
              'type /= "submit",
              "remove",
              event('click) { access =>
                for {
                  _ <- access.transition(state => state.dropRenderer(name))
                } yield ()
              }
            )
          else Document.Empty

        renderers.map(
          renderer =>
            'section (
              renderer.render(state, name),
          )) :::
          List(
          addButton,
          removeButton
        )
      }

      override def value(access: context.Access, name: String): F[String] =
        for {
          state <- access.state
          renderers = state.listRenderers
            .getOrElse(name, Nil)
            .map(_.asInstanceOf[ComplexRenderer[F, T]])
          values <- renderers.map(_.value(access, name)).sequence
        } yield s"[${values.mkString(",")}]"
    }

  implicit def enumRenderer[F[_]: MonadThrow, T <: EnumEntry with NamedEnumEntry](
      implicit context: Context[F],
      enum: Enum[T]): ComplexRenderer[F, T] = new ComplexRenderer[F, T] {
    import context.symbolDsl._

    val elementId = context.elementId()

    override def render(state: AppState, name: String): List[DocumentNode[F]] =
      List(
        'section (
          'label (
            'select (
              elementId,
              enum.values.map(_.name).map(n => 'option ('value /= n, n))
            )
          ))
      )

    override def value(access: context.Access, name: String): F[String] =
      access.valueOf(elementId).map(v => s""" "$v" """)
  }
}
