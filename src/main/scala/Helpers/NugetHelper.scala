package Helpers

import java.io.{InputStream}

import model.{DownloadedFile, NugetPackage}
import java.util.regex.Pattern
import java.util.Scanner
import java.util.zip.ZipFile

object NugetHelper {

  def getNugetPackageDetails(file: DownloadedFile): NugetPackage ={

    val zipFile = new ZipFile(file.localPath)

    val entries = zipFile.entries

    var id : String = ""
    var version : String = ""
    var isFeaturePackage : Boolean = true

    while ( { entries.hasMoreElements }) {
      val entry = entries.nextElement
      if (!entry.getName.isEmpty && entry.getName.endsWith(".nuspec")) {
        val inputStream = zipFile.getInputStream(entry)
        val nuspec = convertStreamToString(inputStream)
        id = getXmlValue(nuspec, "id")
        version = getXmlValue(nuspec, "version")
        isFeaturePackage = checkFeaturePackage(version)
      }
    }

    new NugetPackage(id, version, isFeaturePackage, file)
  }

  private def getXmlValue(source: String, tag: String): String = {
    val pattern = Pattern.compile(String.format("<%s>(.+?)</%s>", tag, tag))
    val matcher = pattern.matcher(source)
    matcher.find
    matcher.group(1)
  }

  private def checkFeaturePackage(version: String): Boolean = {
    version.contains("-") && !version.contains("-RC-")
  }

  private def convertStreamToString(is: InputStream): String = {
    val s = new Scanner(is).useDelimiter("\\A")
    if (s.hasNext) s.next
    else ""
  }
}
