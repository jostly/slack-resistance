package acceptance.api.game

import fixture.{CreateGame, AbstractAcceptanceTest}
import slackres.playcontext.domain.{User, GameId}
import slackres.playcontext.event.{PlayerJoinedEvent, GameCreatedEvent, GameEndedEvent}
import spray.http.StatusCodes

import scala.concurrent.duration._

class EndGameSpec extends AbstractAcceptanceTest with CreateGame {

  feature("End a game") {
    scenario("successfully ending a game") {
      Given("a started game in the channel")
      createGame("scenario_one")

      When("the 'end !' command is posted")
      val reply = slackCommand("end !", channel_id = "scenario_one")

      Then("the server responds with 'OK'")
      reply.status should be (StatusCodes.OK)

      And("'GameEndedEvent' should have occured")
      expectMsgPF() {
        case GameEndedEvent(GameId("scenario_one"), _, _, Some(User("user_name"))) =>
      }
    }
    scenario("ending a game that does not exist") {
      Given("no game started in the channel")

      When("the 'end !' command is posted")
      val reply = slackCommand("end !", channel_id = "scenario_two")

      Then("the server responds with 'Conflict'")
      reply.status should be (StatusCodes.Conflict)

      And("no 'GameEndedEvent' should have occured")
      expectNoMsg(100.milliseconds)
    }
    scenario("ending a game that is already ended") {
      Given("one game started in the channel")
      createGame("scenario_three")

      And("the game is ended")
      slackCommand("end !", channel_id = "scenario_three").status should be (StatusCodes.OK)
      expectMsgClass(classOf[GameEndedEvent])

      When("the 'end !' command is posted")
      val reply = slackCommand("end !", channel_id = "scenario_three")

      Then("the server responds with 'Conflict'")
      reply.status should be (StatusCodes.Conflict)

      And("no 'GameEndedEvent' should have occured")
      expectNoMsg(100.milliseconds)
    }
    scenario("trying to end a game without ! in the command") {
      Given("a started game in the channel")
      createGame("scenario_4")

      When("the 'end' command is posted")
      val reply = slackCommand("end", channel_id = "scenario_4")

      Then("the server responds with 'RetryWith'")
      reply.status should be (StatusCodes.RetryWith)

      And("no 'GameEndedEvent' should have occured")
      expectNoMsg(100.milliseconds)
    }

  }

}
