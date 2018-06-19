package actors.slack

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import config.Settings
import model.{SlackFile, UploadCompleted}
import slack.rtm.SlackRtmClient
import slack.models.MessageSubtypes.FileShareMessage
import slack.models.MessageWithSubtype

class SlackChannelActor(val settings : Settings) extends Actor with ActorLogging {

  implicit val system = context.system
  private val client = SlackRtmClient(settings.slackApiKey)

  override def receive: Receive = {
    case "start listen slack" => {
      client.onEvent(event => {
        event match {
          case e: MessageWithSubtype => {
            e.messageSubType match {
              case attachment: FileShareMessage => {
                val file = new SlackFile(attachment.file.name.get, attachment.file.url_private_download.get)
                val slackFileActor : ActorRef = context.system.actorOf(Props(classOf[SlackFileActor], e.channel, settings))
                slackFileActor ! file
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
