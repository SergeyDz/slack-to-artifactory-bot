package actors.nuget

import Helpers.NugetHelper
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import config.Settings
import model.DownloadedFile

class NugetParseActor(val settings : Settings) extends Actor with ActorLogging {
  override def receive: Receive = {
    case file: DownloadedFile => {
      val nuget = NugetHelper.getNugetPackageDetails(file)
      sender ! nuget
    }
  }
}
