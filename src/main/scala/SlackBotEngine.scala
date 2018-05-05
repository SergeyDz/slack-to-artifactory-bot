import akka.actor.ActorSystem
import slack.SlackUtil
import slack.rtm.SlackRtmClient

object SlackBotEngine {

  implicit val system = ActorSystem("slack")
  implicit val ec = system.dispatcher
  val slackApiKey : String = sys.env("slackkey")

  def Run() = {

    val client = SlackRtmClient(slackApiKey)
    val selfId = client.state.self.id

    client.onMessage { message =>
      val mentionedIds = SlackUtil.extractMentionedIds(message.text)

      if(mentionedIds.contains(selfId)) {
        client.sendMessage(message.channel, s"<@${message.user}>: Hey!")
      }
    }
  }
}
