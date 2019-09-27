package ru.tinkoff.traveladmin.access

import enumeratum.{CirceEnum, Enum, EnumEntry}
import ru.tinkoff.traveladmin.access.AdminOperation._

sealed trait Role extends EnumEntry {
  def operations: List[AdminOperation]
}

object Role extends Enum[Role] with CirceEnum[Role] {

  val values = findValues

  case object Admin extends Role {
    def operations =
      List(foo, bar)
  }
}
