package acceptance.api.game

import fixture.AbstractAcceptanceTest
import slackres.playcontext.domain.{User, GameId}
import slackres.playcontext.event.{PlayerJoinedEvent, GameEndedEvent, GameCreatedEvent}
import spray.http.StatusCodes
import scala.concurrent.duration._

class JoinGameSpec extends AbstractAcceptanceTest {

  feature("Join a game") {
    scenario("successfully joining a game") {
      Given("a started game in the channel")
      slackCommand("create", channel_id = "scenario_one").status should be (StatusCodes.OK)
      expectMsgClass(classOf[GameCreatedEvent])

      When("the 'join' command is posted")
      val reply = slackCommand("join", channel_id = "scenario_one", user_name = "joiner")

      Then("the server responds with 'OK'")
      reply.status should be (StatusCodes.OK)

      And("'PlayerJoinedEvent' should have occured")
      expectMsgPF() {
        case PlayerJoinedEvent(GameId("scenario_one"), _, _, User("joiner")) =>
      }
    }
  }

}
