package fixture

import spray.http.FormData

case class SlackSlashCommand(token: String = "token",
                             team_id: String = "team_id",
                             team_domain: String = "team_domain",
                             channel_id: String = "channel_id",
                             channel_name: String = "channel_name",
                             user_id: String = "user_id",
                             user_name: String = "user_name",
                             command: String = "/res",
                             text: String) {
}
