package bot

import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, Props}
import actors.slack.SlackActor
import config.Settings
import contracts.Shutdownable

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object SlackBotEngine extends Shutdownable {
  implicit val system = ActorSystem("slack")
  private val settings : Settings = Settings()
  private val slackBot = system.actorOf(Props(classOf[SlackActor], settings), "slack-bot")

  def Run() = {

    slackBot ! "start echo"
    slackBot ! "start files"

    sys.addShutdownHook(shutdown())
  }

  override def shutdown(): Unit = {
    system.terminate()
    Await.ready(system.whenTerminated, Duration(1, TimeUnit.MINUTES))
    println("Slack Bot Engine shutdown completed.")
  }
}
