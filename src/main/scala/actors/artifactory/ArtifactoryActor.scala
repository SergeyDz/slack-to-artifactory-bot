package actors.artifactory

import java.io.File

import akka.actor.{Actor, ActorLogging}
import config.Settings
import model.{UploadCompleted, NugetPackage}
import org.jfrog.artifactory.client.ArtifactoryClient

class ArtifactoryActor(val settings : Settings)  extends Actor with ActorLogging {
  val artifactory = ArtifactoryClient.create(settings.artifactoryBaseUrl, settings.artifactoryUser, settings.artifactoryPassword)

  override def receive: Receive = {
    case nuget: NugetPackage => {
      val targetRepository  : String = if(nuget.isFeaturePackage) "SBTechFeature" else "SBTech"
      val isExists = checkIfPackageExistsInArtifactory(nuget, targetRepository)

      if (!isExists) {
        val fullUrl = String.format("%s/webapp/#/artifacts/browse/tree/General/%s/%s/%s", this.settings.artifactoryBaseUrl, targetRepository, nuget.id, nuget.file.slack.name)
        artifactory.repository(targetRepository).upload(String.format("%s/%s", nuget.id, nuget.file.slack.name), new File(nuget.file.localPath)).doUpload
        sender ! new UploadCompleted(nuget.file.slack.name, nuget.file.slack.channel, s":artifactory: package upload to ${targetRepository} completed: <${fullUrl}|${nuget.file.slack.name}>", nuget.file.slack.url)
      }
      else {
        log.warning(s":no_entry_sign: package already exists: ${nuget.file.slack.name}")
        sender ! new UploadCompleted(nuget.file.slack.name, nuget.file.slack.channel, s":no_entry_sign: package already exists: ${nuget.file.slack.name}", nuget.file.slack.url)
      }
    }
  }

  private def checkIfPackageExistsInArtifactory(nuget: NugetPackage, targetRepository : String) : Boolean = {
    !artifactory.searches.repositories(targetRepository).artifactsByName(nuget.file.slack.name).doSearch.isEmpty
  }
}
