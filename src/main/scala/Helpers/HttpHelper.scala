package Helpers

import config.Settings
import model.{DeleteSlackFile, SlackFile}
import java.net.{HttpURLConnection, URL}
import java.io.File

object HttpHelper {
  def DownloadFile(file : SlackFile, settings : Settings): String ={
    val key : String = "Bearer " + settings.slackApiKey
    val requestProperties = Map(
      "Authorization" -> key
    )
    val connection = new URL(file.url).openConnection
    requestProperties.foreach({
      case (name, value) => connection.setRequestProperty(name, value)
    })

    val localFile = new File(System.getProperty("java.io.tmpdir") + file.name).getCanonicalPath()
    inputStreamToFile(connection.getInputStream, new File(localFile))
    localFile
  }

  def DeleteFile(file : DeleteSlackFile, settings : Settings) ={
    val key : String = "Bearer " + settings.slackApiKey
    val requestProperties = Map(
      "Authorization" -> key
    )
    val connection : HttpURLConnection = new URL(file.url).openConnection.asInstanceOf[HttpURLConnection]
    requestProperties.foreach({
      case (name, value) => connection.setRequestProperty(name, value)
    })
    connection.setRequestMethod("DELETE")
  }

  private def inputStreamToFile(inputStream: java.io.InputStream, file: java.io.File) = {
    val fos = new java.io.FileOutputStream(file)
    fos.write(
      Stream.continually(inputStream.read).takeWhile(-1 !=).map(_.toByte).toArray
    )
    fos.close()
  }
}
