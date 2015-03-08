package slackres.playcontext.domain

import org.scalatest.{Matchers, FunSuite}
import slackres.playcontext.event.{GameEndedEvent, PlayerJoinedEvent, GameSettings, GameCreatedEvent}

class GameTest extends FunSuite with Matchers {
  val creator: User = User("userName")
  val player = User("anotherPlayer")
  val gameId: GameId = GameId("gameId")
  val maxPlayers: Int = 5


  test("creating sets properties") {
    val game = Game.create(gameId, creator, maxPlayers)

    game should have (
      'id (gameId),
      'creator (creator),
      'maxNumberOfPlayers (maxPlayers),
      'state (Game.PlayersJoining)
    )
  }

  test("creating adds creator to player list") {
    val game = Game.create(gameId, creator, maxPlayers)

    game should have ('players (List(creator)))
  }

  test("free seats is calculated from max players") {
    val game1 = Game.create(gameId, creator, maxPlayers)
    game1 should have ('freeSeats (maxPlayers-1))

    val game2 = Game.create(gameId, creator, maxPlayers + 1)
    game2 should have ('freeSeats (maxPlayers))
  }

  test("creating fails if state is PlayersJoining") {
    val game = new Game()
    game.state = Game.PlayersJoining

    an [IllegalStateException] should be thrownBy game.create(gameId, creator, maxPlayers)
  }

  test("creating fails if state is Playing") {
    val game = new Game()
    game.state = Game.Playing

    an [IllegalStateException] should be thrownBy game.create(gameId, creator, maxPlayers)
  }

  test("creating succeeds if state is Ended") {
    val game = new Game()
    game.state = Game.Ended

    game.create(gameId, creator, maxPlayers)
    game should have (
      'id (gameId),
      'creator (creator),
      'maxNumberOfPlayers (maxPlayers),
      'state (Game.PlayersJoining)
    )
  }

  test("creating emits GameCreatedEvent") {
    val game = Game.create(gameId, creator, maxPlayers)

    val gce = game.uncommittedEvents().filter(_.isInstanceOf[GameCreatedEvent])

    gce should have length 1
    gce.head should have (
      'aggregateId (gameId),
      'createdBy (creator),
      'settings (GameSettings(maxPlayers))
    )
  }

  test("creating emits PlayerJoinedEvent") {
    val game = Game.create(gameId, creator, maxPlayers)

    val pje = game.uncommittedEvents().filter(_.isInstanceOf[PlayerJoinedEvent])

    pje should have length 1
    pje.head should have (
      'aggregateId (gameId),
      'player (creator)
    )
  }

  test("joining adds player to list") {
    val game = Game.create(gameId, creator, maxPlayers)

    game.addPlayer(player)

    game should have ('players (List(player, creator)))
  }

  test("joining adjusts freeSeats") {
    val game = Game.create(gameId, creator, maxPlayers)

    game should have ('freeSeats (4))

    game.addPlayer(player)

    game should have ('freeSeats (3))
  }

  test("cannot join a game in the wrong state") {
    val game = Game.create(gameId, creator, maxPlayers)
    game.state = Game.Playing

    an [IllegalStateException] should be thrownBy game.addPlayer(player)
  }

  test("cannot join a game with too few free seats") {
    val game = Game.create(gameId, creator, 1)

    an [IllegalStateException] should be thrownBy game.addPlayer(player)
  }

  test("cannot join a game more than once") {
    val game = Game.create(gameId, creator, maxPlayers)

    an [IllegalStateException] should be thrownBy game.addPlayer(creator)
  }

  test("joining emits PlayerJoinedEvent") {
    val game = Game.create(gameId, creator, maxPlayers)

    game.markChangesAsCommitted()

    game.addPlayer(player)

    val pje = game.uncommittedEvents().filter(_.isInstanceOf[PlayerJoinedEvent])

    pje should have length 1
    pje.head should have (
      'aggregateId (gameId),
      'player (player)
    )
  }

  test("ending sets state") {
    val game = Game.create(gameId, creator, maxPlayers)

    game.end(player)

    game should have ('state (Game.Ended))
  }

  test("cannot end already ended game") {
    val game = Game.create(gameId, creator, maxPlayers)

    game.end(player)

    an [IllegalStateException] should be thrownBy game.end(player)
  }

  test("ending emits GameEndedEvent") {
    val game = Game.create(gameId, creator, maxPlayers)

    game.end(player)
    val ge = game.uncommittedEvents().filter(_.isInstanceOf[GameEndedEvent])

    ge should have length 1
    ge.head should have (
      'aggregateId (gameId),
      'endedBy (Some(player)))
  }

  test("creating from an ended game resets all properties") {
    val game = Game.create(gameId, creator, maxPlayers)
    game.end(creator)

    game.markChangesAsCommitted()

    game.create(gameId, player, 8)

    game should have (
      'state (Game.PlayersJoining),
      'id (gameId),
      'creator (player),
      'maxNumberOfPlayers (8),
      'players (List(player))
    )
  }

}
