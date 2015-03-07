package slackres.playcontext.resource

import parser.{StatusCodeAndMessageResponse, Parser, SlackCommand}
import slackres.playcontext.command.{EndGameCommand, CommandHandler, CreateGameCommand}
import slackres.playcontext.domain.{GameId, User}

class CommandParser(commandHandler: CommandHandler) extends Parser {

  override def parse(input: SlackCommand) = {
    case "create" :: _ =>
      val command = CreateGameCommand(GameId(input.channelId), User(input.userName))
      commandHandler.handle(command)
    case "end" :: _ =>
      val command = EndGameCommand(GameId(input.channelId))
      commandHandler.handle(command)
  }

}
