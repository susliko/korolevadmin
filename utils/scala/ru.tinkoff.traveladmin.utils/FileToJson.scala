package ru.tinkoff.traveladmin.utils

import fs2.Stream

trait FileToJson[F[_], T] {
  def convert(filename: String, stream: Stream[F, String]): F[String]
}
