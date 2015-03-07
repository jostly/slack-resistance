package slackres.playcontext.infrastructure

import akka.event.EventStream
import com.typesafe.scalalogging.LazyLogging
import domain.{AggregateRoot, GenericId, Repository}
import event.DomainEventStore

class DefaultRepository(eventBus: EventStream, domainEventStore: DomainEventStore) extends Repository with LazyLogging {

  override def save[ID <: GenericId, AR <: AggregateRoot[ID]](aggregateRoot: AR): Unit = {
    if (aggregateRoot.hasUncommittedEvents) {
      val newEvents = aggregateRoot.uncommittedEvents()

      domainEventStore.save(aggregateRoot.id, aggregateRoot.getClass, newEvents)

      newEvents.foreach(eventBus.publish(_))

      aggregateRoot.markChangesAsCommitted()
    }
  }

  override def load[ID <: GenericId, AR <: AggregateRoot[ID]](id: ID, aggregateType: Class[AR]): Option[AR] = {
    try {
      val aggregateRoot: AR = aggregateType.newInstance
      aggregateRoot.loadFromHistory(domainEventStore.loadEvents(id))
      Some(aggregateRoot)
    }
    catch {
      case iae: IllegalArgumentException =>
        logger.debug(s"Aggregate of type ${aggregateType.getSimpleName} does not exist, ID: $id")
        None
    }
  }
}
