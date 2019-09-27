package ru.tinkoff.traveladmin.utils

object tuples {
  implicit class Func1Ops[X, Y](f: X => Y) {
    def tupled: Tuple1[X] => Y = x => f(x._1)
  }
}
