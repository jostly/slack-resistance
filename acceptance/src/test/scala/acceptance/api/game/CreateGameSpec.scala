package acceptance.api.player

import fixture.AbstractAcceptanceTest
import slackres.playcontext.domain.{User, GameId}
import slackres.playcontext.event.{GameEndedEvent, GameCreatedEvent}
import spray.http.StatusCodes
import scala.concurrent.duration._

class CreateGameSpec extends AbstractAcceptanceTest {

  feature("Create a game") {
    scenario("successfully creating a game") {
      Given("no started game in the channel")

      When("the 'create' command is posted")
      val reply = slackCommand("create", channel_id = "scenario_one")

      Then("the server responds with 'OK'")
      reply.status should be (StatusCodes.OK)

      And("'GameCreatedEvent' should have occured")
      expectMsgPF() {
        case GameCreatedEvent(GameId("scenario_one"), _, _, User("user_name")) =>
      }
    }
    scenario("creating a game when one is running") {
      Given("one game started in the channel")
      slackCommand("create", channel_id = "scenario_two").status should be (StatusCodes.OK)
      expectMsgClass(classOf[GameCreatedEvent])

      When("the 'create' command is posted")
      val reply = slackCommand("create", channel_id = "scenario_two")

      Then("the server responds with 'Conflict'")
      reply.status should be (StatusCodes.Conflict)

      And("No event should have occured")
      expectNoMsg(100.milliseconds)
    }
    scenario("creating a game when one has been ended") {
      Given("one game started in the channel")
      slackCommand("create", channel_id = "scenario_three").status should be (StatusCodes.OK)
      expectMsgClass(classOf[GameCreatedEvent])

      And("the game is ended")
      slackCommand("end !", channel_id = "scenario_three").status should be (StatusCodes.OK)
      expectMsgClass(classOf[GameEndedEvent])

      When("the 'create' command is posted")
      val reply = slackCommand("create", channel_id = "scenario_three")

      Then("the server responds with 'OK'")
      reply.status should be (StatusCodes.OK)

      And("'GameCreatedEvent' should have occured")
      expectMsgPF() {
        case GameCreatedEvent(GameId("scenario_three"), _, _, User("user_name")) =>
      }

    }
  }

}
