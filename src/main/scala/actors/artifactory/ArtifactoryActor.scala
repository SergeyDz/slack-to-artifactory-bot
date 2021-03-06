package actors.artifactory

import java.io.File

import akka.actor.{Actor, ActorLogging}
import config.Settings
import model.{DeleteSlackFile, NugetPackage, UploadCompleted}
import org.jfrog.artifactory.client.ArtifactoryClient

class ArtifactoryActor(val settings : Settings)  extends Actor with ActorLogging {
  val artifactory = ArtifactoryClient.create(settings.artifactoryBaseUrl, settings.artifactoryUser, settings.artifactoryPassword)

  override def receive: Receive = {
    case nuget: NugetPackage => {
      val targetRepository  : String = if(nuget.isFeaturePackage) "SBTechFeature" else "SBTech"
      val isExists = checkIfPackageExistsInArtifactory(nuget, targetRepository)

      sender ! new DeleteSlackFile(nuget.file.slack.url)

      if (!isExists) {
        val fullUrl = buildNugetPackageArtifactoryPath(targetRepository, nuget)
        artifactory.repository(targetRepository).upload(String.format("%s/%s", nuget.id, nuget.file.slack.name), new File(nuget.file.localPath)).doUpload
        sender ! new UploadCompleted(s":artifactory: package upload to ${targetRepository} completed: ${fullUrl}", null)
      }
      else {
        log.warning(s":no_entry_sign: package already exists: ${nuget.file.slack.name}")
        sender ! new UploadCompleted(s":no_entry_sign: package already exists: ${nuget.file.slack.name}", null)
      }
    }
  }

  private def checkIfPackageExistsInArtifactory(nuget: NugetPackage, targetRepository : String) : Boolean = {
    !artifactory.searches.repositories(targetRepository).artifactsByName(nuget.file.slack.name).doSearch.isEmpty
  }

  private def buildNugetPackageArtifactoryPath(targetRepository : String, nuget : NugetPackage): String ={
    String.format("%s/webapp/#/artifacts/browse/tree/General/%s/%s/%s", this.settings.artifactoryBaseUrl, targetRepository, nuget.id, nuget.file.slack.name)
  }
}
