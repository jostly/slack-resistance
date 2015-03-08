package slackres.playcontext.query

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.testkit.{TestActorRef, TestKit}
import akka.util.Timeout
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSuiteLike, Matchers}
import slackres.playcontext.domain.{GameId, GameState, GameStatus, User}
import slackres.playcontext.event.{GameCreatedEvent, GameEndedEvent, GameSettings, PlayerJoinedEvent}
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Success

class GameStatusTrackerTest extends TestKit(ActorSystem()) with FunSuiteLike with Matchers with MockFactory {

  val id1 = GameId("id1")
  val id2 = GameId("id2")
  val user1 = User("user1")

  test("GameCreatedEvent") {
    val repo = mock[GameStatusRepository]
    val tracker = TestActorRef(Props(classOf[GameStatusTracker], repo))

    repo.save _ expects GameStatus(id1, 5, Set.empty, GameState.PlayersJoining)

    tracker ! GameCreatedEvent(id1, 0, 0, user1, GameSettings(5))
  }

  test("PlayerJoinedEvent") {
    val repo = mock[GameStatusRepository]
    val tracker = TestActorRef(Props(classOf[GameStatusTracker], repo))

    repo.load _ expects id1 returning Some(GameStatus(id1, 5, Set.empty, GameState.PlayersJoining))

    repo.save _ expects GameStatus(id1, 5, Set(user1), GameState.PlayersJoining)

    tracker ! PlayerJoinedEvent(id1, 0, 0, user1)
  }

  test("GameEndedEvent") {
    val repo = mock[GameStatusRepository]
    val tracker = TestActorRef(Props(classOf[GameStatusTracker], repo))

    repo.load _ expects id1 returning Some(GameStatus(id1, 5, Set(user1), GameState.PlayersJoining))

    repo.save _ expects GameStatus(id1, 5, Set(user1), GameState.Ended)

    tracker ! GameEndedEvent(id1, 0, 0)
  }

  test("Respond with status directly from repository") {
    val repo = mock[GameStatusRepository]
    val tracker = TestActorRef(Props(classOf[GameStatusTracker], repo))
    val status: GameStatus = GameStatus(id1, 5, Set(user1), GameState.PlayersJoining)

    repo.load _ expects id1 returning Some(status)
    repo.load _ expects id2 returning None

    implicit val timeout = Timeout(1.second)

    gameStatusOption(tracker ? id1) should be (Some(status))
    gameStatusOption(tracker ? id2) should be (None)
  }

  def gameStatusOption(future: Future[Any]) = {
    val Success(result: Option[GameStatus]) = future.value.get
    result
  }
}
