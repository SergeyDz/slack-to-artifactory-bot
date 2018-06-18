package model

case class NugetPackage(id: String, version: String, isFeaturePackage: Boolean, file: DownloadedFile)
