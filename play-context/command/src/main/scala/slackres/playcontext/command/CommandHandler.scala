package slackres.playcontext.command

import com.typesafe.scalalogging.LazyLogging
import command.Command
import domain.Repository
import parser.{ParseResponse, StatusCodeAndMessageResponse, StatusCodeResponse}
import slackres.playcontext.domain.Game._
import slackres.playcontext.domain.{User, GameId, Game}
import spray.http.StatusCodes._

case class CreateGameCommand(id: GameId, user: User, numberOfPlayers: Int) extends Command
case class EndGameCommand(id: GameId, user: User) extends Command
case class JoinGameCommand(id: GameId, user: User) extends Command

class CommandHandler(repository: Repository) extends LazyLogging {

  def handle(command: Command): ParseResponse = command match {
    case CreateGameCommand(gameId, user, maxPlayers) =>
      repository.load(gameId, classOf[Game]) match {
        case Some(game) if game.state != Ended =>
          StatusCodeAndMessageResponse(Conflict, "There is already a game running in this channel")
        case _ =>
          val game = Game.create(gameId, user, maxPlayers)
          repository.save[GameId, Game](game)
          StatusCodeAndMessageResponse(OK, "Game started")
      }
    case EndGameCommand(gameId, user) =>
      repository.load(gameId, classOf[Game]) match {
        case Some(game) if game.state != Ended =>
          game.end(user)
          repository.save[GameId, Game](game)
          StatusCodeAndMessageResponse(OK, "Game ended")
        case _ =>
          StatusCodeAndMessageResponse(Conflict, "There is no game currently running in this channel.")
      }
    case JoinGameCommand(gameId, user) =>
      repository.load(gameId, classOf[Game]) match {
        case Some(game) if game.state == PlayersJoining =>
          game.addPlayer(user)
          repository.save[GameId, Game](game)
          StatusCodeAndMessageResponse(OK, "Joined game")
        case _ =>
          StatusCodeAndMessageResponse(Conflict, "There is no game running in this channel that you can join.")
      }

    case _ => StatusCodeResponse(BadRequest)
  }

}
