package slackres.playcontext.event

import event.DomainEvent
import slackres.playcontext.domain.{User, GameId}

case class GameCreatedEvent(aggregateId: GameId, version: Int, timestamp: Long, createdBy: User)
  extends DomainEvent[GameId]
