package fixture

import java.util.UUID

trait UUIDGenerator {
  private[this] var idCounter: Long = 0

  def nextId(): String = {
    idCounter += 1
    new UUID(0, idCounter).toString
  }

  def randomId(): String = UUID.randomUUID().toString
}
