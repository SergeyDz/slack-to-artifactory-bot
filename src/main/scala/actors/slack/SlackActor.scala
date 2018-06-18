package actors.slack

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import bot.SlackBotEngine.system
import config.Settings
import model.{SlackFile, UploadCompleted}
import slack.SlackUtil
import slack.rtm.SlackRtmClient
import slack.models.MessageSubtypes.FileShareMessage
import slack.models.MessageWithSubtype

class SlackActor(val settings : Settings) extends Actor with ActorLogging {

  implicit val system = context.system
  private val client = SlackRtmClient(settings.slackApiKey)

  override def receive: Receive = {
    case "start echo" => {
      val selfId = client.state.self.id
      client.onMessage { message =>
        val mentionedIds = SlackUtil.extractMentionedIds(message.text)

        if(mentionedIds.contains(selfId)) {
          client.sendMessage(message.channel,s"<@${message.user}>: Hey!")
        }
      }
    }
    case "start files" => {
      client.onEvent(event => {
        event match {
          case e: MessageWithSubtype => {
            e.messageSubType match {
              case attachment: FileShareMessage => {
                val file = new SlackFile(attachment.file.name.get, attachment.file.url_private_download.get, e.channel)
                val slackFileActor : ActorRef = context.system.actorOf(Props(classOf[SlackFileActor], settings))
                slackFileActor ! file
                //to delete client.sendMessage(e.channel,s"<@${e.user}> added a file ${attachment.file.name.get}")
              }
              case _ =>
            }
          }
          case _ =>
        }
      })
    }
    case completed: UploadCompleted => {
      client.sendMessage(completed.channel, completed.message)
    }
  }
}
