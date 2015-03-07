package slackres.playcontext.resource

import parser.{Parser, SlackCommand, StatusCodeAndMessageResponse}
import slackres.playcontext.domain.GameState.{Ended, Playing, PlayersJoining}
import slackres.playcontext.domain.{GameStatus, GameId}
import slackres.playcontext.query.GameStatusRepository
import spray.http.StatusCodes

class QueryParser(repository: GameStatusRepository) extends Parser {

  override def parse(input: SlackCommand) = {
    case "status" :: _ =>
      repository.load(GameId(input.channelId)) match {
        case Some(GameStatus(_, _, players, state)) =>
          val playerNames = players.map(_.name).mkString(", ")
          val gameState = state match {
            case PlayersJoining => "waiting for more players"
            case Playing => "running"
            case Ended => "over"
          }
          StatusCodeAndMessageResponse(StatusCodes.OK, s"The game is $gameState, with players $playerNames")
        case None =>
          StatusCodeAndMessageResponse(StatusCodes.UnprocessableEntity, "No game in this channel.")
      }

  }

}