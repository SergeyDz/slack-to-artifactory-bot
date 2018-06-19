package actors.slack

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import config.Settings
import model._

class SlackFileActor(val channel : String, val settings : Settings) extends Actor with ActorLogging {
  var slackChannelActor : ActorRef = null
  override def receive: Receive = {
    case file : SlackFile => {
      // detect sender here
      slackChannelActor = sender
      slackChannelActor ! new UploadCompleted(channel, "Done")
    }
  }
}
