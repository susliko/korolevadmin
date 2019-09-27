package ru.tinkoff.traveladmin.access

import enumeratum.{Enum, EnumEntry}

sealed trait AdminOperation extends EnumEntry

object AdminOperation extends Enum[AdminOperation] {

  def values = findValues

  def unsafeByName(name: String): AdminOperation = namesToValuesMap(name)
  case object foo extends AdminOperation
  case object bar extends AdminOperation
}
