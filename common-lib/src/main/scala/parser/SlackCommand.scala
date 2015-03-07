package parser

class SlackCommand(map: Map[String, String]) {

  def userName = map("user_name")
  def userId = map("user_id")
  def channelName = map("channel_name")
  def channelId = map("channel_id")
  def token = map("token")
  def teamDomain = map("team_domain")
  def teamId = map("team_id")
  def text = map("text").split(" ").toList

}
