package actors.slack

import Helpers.HttpHelper
import actors.artifactory.ArtifactoryActor
import actors.nuget.NugetParseActor
import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import config.Settings
import model._

class SlackFileActor(val channel : String, val settings : Settings) extends Actor with ActorLogging {
  var slackChannelActor : ActorRef = null

  override def receive: Receive = {
    case file : SlackFile => {
      slackChannelActor = sender
      val localPath : String = HttpHelper.DownloadFile(file, settings)
      val downloaded = new DownloadedFile(localPath, file)
      val nugetActor : ActorRef = context.system.actorOf(Props(classOf[NugetParseActor], settings))
      nugetActor ! downloaded
    }
    case nuget: NugetPackage => {
      log.info(s"Package ${nuget.id} version=${nuget.version} detected.")
      val artifactoryActor : ActorRef = context.system.actorOf(Props(classOf[ArtifactoryActor], settings))
      artifactoryActor ! nuget
    }
    case delete: DeleteSlackFile => {
      HttpHelper.DeleteFile(delete, settings)
    }
    case completed: UploadCompleted => {
      completed.channel = channel
      slackChannelActor ! completed
    }
  }
}
