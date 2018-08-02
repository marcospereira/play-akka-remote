name := """play-akka-remote"""
organization := "com.lightbend"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.12.6"

libraryDependencies += guice

// Add Akka Remote dependency to all Play project
libraryDependencies += "com.typesafe.akka" %% "akka-remote" % "2.5.13"

javacOptions ++= Seq("-Xlint:deprecation")
