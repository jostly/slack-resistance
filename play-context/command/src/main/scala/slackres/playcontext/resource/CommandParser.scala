package slackres.playcontext.resource

import parser.{StatusCodeAndMessageResponse, Parser, SlackCommand}
import slackres.playcontext.command.{JoinGameCommand, EndGameCommand, CommandHandler, CreateGameCommand}
import slackres.playcontext.domain.{GameId, User}
import spray.http.StatusCodes

class CommandParser(commandHandler: CommandHandler) extends Parser {

  override def parse(input: SlackCommand) = {
    case "create" :: Nil =>
      commandHandler.handle(CreateGameCommand(GameId(input.channelId), User(input.userName), 10))
    case "create" :: num :: Nil =>
      commandHandler.handle(CreateGameCommand(GameId(input.channelId), User(input.userName), num.toInt))
    case "end" :: Nil =>
      StatusCodeAndMessageResponse(StatusCodes.RetryWith, "Are you sure you want to end this game? Use the command 'end !' to end it.")
    case "end" :: "!" :: Nil =>
      commandHandler.handle(EndGameCommand(GameId(input.channelId), User(input.userName)))

    case "join" :: Nil =>
      commandHandler.handle(JoinGameCommand(GameId(input.channelId), User(input.userName)))
  }

}
