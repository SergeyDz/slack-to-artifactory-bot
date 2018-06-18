package config

import com.typesafe.config.ConfigFactory

case class Settings(slackApiKey : String, artifactoryBaseUrl : String, artifactoryUser : String, artifactoryPassword : String){
  import Settings._
}

object Settings {
  def apply(): Settings = {
    val slackApi =  sys.env("slackkey")
    val artifactoryUrl : String = ConfigFactory.load().getString("bot.artifactory.url")
    val artifactoryUser : String =  sys.env("afuser")
    val artifactoryPassword : String =  sys.env("afpassword")
    new Settings(slackApi, artifactoryUrl, artifactoryUser, artifactoryPassword)
  }
}
