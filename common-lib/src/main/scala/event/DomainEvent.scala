package event

import domain.GenericId

trait DomainEvent[T <: GenericId] {

  val aggregateId: T
  val version: Int
  val timestamp: Long

}
