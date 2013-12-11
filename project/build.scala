import sbt._

import Keys._
import AndroidKeys._

object General {
  val settings = Defaults.defaultSettings ++ Seq (
    name := "ScalaInstaller",
    version := "0.1",
    scalaVersion := "2.9.1",
    platformName in Android := "android-8",
    javacOptions ++= Seq("-source", "1.6", "-target", "1.6")
  )

  lazy val fullAndroidSettings =
    General.settings ++  
    AndroidProject.androidSettings ++
    TypedResources.settings ++
    AndroidMarketPublish.settings ++ Seq (
      keyalias in Android := "scala_installer",
      keystorePath in Android := file("../keys/scala_installer.keystore"),
      libraryDependencies += "org.scalatest" %% "scalatest" % "1.6.1" % "test"
    )
}

object AndroidBuild extends Build {
  lazy val main = Project (
    "ScalaInstaller",
    file("."),
    settings = General.fullAndroidSettings
  )

  lazy val tests = Project (
    "tests",
    file("tests"),
    settings = General.settings ++ AndroidTest.androidSettings ++ Seq (
      name := "ScalaInstallerTests"
    )
  ) dependsOn main
}
