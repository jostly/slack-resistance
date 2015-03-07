package event

import domain.{AggregateRoot, GenericId}

trait DomainEventStore {
  def loadEvents[ID <: GenericId, DE <: DomainEvent[ID]](id: ID): List[DE]

  def save[ID <: GenericId, AR <: AggregateRoot[ID], DE <: DomainEvent[ID]](id: ID, aggregateType: Class[AR], events: List[DE])

  def getAllEvents: List[DomainEvent[_ <: GenericId]]
}