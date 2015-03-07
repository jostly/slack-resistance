package slackres.playcontext.resource

import parser.{Parser, SlackCommand, StatusCodeAndMessageResponse}
import spray.http.StatusCodes

object QueryParser extends Parser {

  override def parse(input: SlackCommand) = {
    case "status" :: _ =>
      StatusCodeAndMessageResponse(StatusCodes.OK, "ORRERY")
  }

}