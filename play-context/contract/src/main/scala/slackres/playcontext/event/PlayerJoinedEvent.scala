package slackres.playcontext.event

import event.DomainEvent
import slackres.playcontext.domain.{User, GameId}

case class PlayerJoinedEvent(aggregateId: GameId, version: Int, timestamp: Long, player: User)
  extends DomainEvent[GameId]