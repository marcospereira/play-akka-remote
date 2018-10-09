lazy val commonSettings = Seq(
  scalaVersion := "2.12.7",
  javacOptions ++= Seq("-Xlint:deprecation"),
  libraryDependencies += "com.typesafe.akka" %% "akka-remote" % "2.5.17",

  organization := "com.lightbend",
  version := "1.0-SNAPSHOT"
)

lazy val root = (project in file(".")).aggregate(
  `play-client-app`,
  `akka-remote-service`,
  `actor-ops`
)

lazy val `actor-ops` = (project in file("actor-ops"))
  .settings(commonSettings: _*)

lazy val `play-client-app` = (project in file("play-client-app"))
  .enablePlugins(PlayJava)
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies += guice
  )
  .dependsOn(`actor-ops`)

lazy val `akka-remote-service` = (project in file("akka-remote-service"))
  .settings(commonSettings: _*)
  .settings(
    mainClass := Some("")
  )
  .dependsOn(`actor-ops`)

