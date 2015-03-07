package acceptance.api.game

import fixture.AbstractAcceptanceTest
import slackres.playcontext.domain.GameId
import slackres.playcontext.event.{GameCreatedEvent, GameEndedEvent}
import spray.http.StatusCodes

import scala.concurrent.duration._

class EndGameSpec extends AbstractAcceptanceTest {

  feature("End a game") {
    scenario("successfully ending a game") {
      Given("a started game in the channel")
      slackCommand("create", channel_id = "scenario_one").status should be (StatusCodes.OK)
      expectMsgClass(classOf[GameCreatedEvent])

      When("the 'end !' command is posted")
      val reply = slackCommand("end !", channel_id = "scenario_one")

      Then("the server responds with 'OK'")
      reply.status should be (StatusCodes.OK)

      And("'GameEndedEvent' should have occured")
      expectMsgPF() {
        case GameEndedEvent(GameId("scenario_one"), _, _) =>
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
      slackCommand("create", channel_id = "scenario_three").status should be (StatusCodes.OK)
      expectMsgClass(classOf[GameCreatedEvent])

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

  }

}
