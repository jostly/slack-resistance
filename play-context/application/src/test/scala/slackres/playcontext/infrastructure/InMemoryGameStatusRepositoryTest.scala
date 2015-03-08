package slackres.playcontext.infrastructure

import org.scalatest.{Matchers, FunSuite}
import slackres.playcontext.domain._

class InMemoryGameStatusRepositoryTest extends FunSuite with Matchers {

  val id1 = GameId("id1")
  val status1 = GameStatus(id1, 5, Set(User("user 1"), User("user 2")), GameState.PlayersJoining)

  val id2 = GameId("id2")
  val status2 = GameStatus(id2, 7, Set(User("user 20")), GameState.Ended)

  def createRepo = new InMemoryGameStatusRepository()

  test("loading from empty repository") {
    val repo = createRepo

    repo.load(id1) should be (None)
  }

  test("saving and loading single") {
    val repo = createRepo

    repo.save(status1)

    repo.load(id1) should be (Some(status1))
    repo.load(id2) should be (None)
  }

  test("saving and loading multiple") {
    val repo = createRepo

    repo.save(status2)
    repo.save(status1)

    repo.load(id1) should be (Some(status1))
    repo.load(id2) should be (Some(status2))
  }

  test("updating") {
    val repo = createRepo

    repo.save(status2)
    repo.save(status1)

    val updatedStatus: GameStatus = status2.copy(state = GameState.PlayersJoining)
    repo.save(updatedStatus)

    repo.load(id1) should be (Some(status1))
    repo.load(id2) should be (Some(updatedStatus))
  }

}
