ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

val AkkaVersion     = "2.7.0"
val AkkaHttpVersion = "10.4.0"

lazy val root = (project in file("."))
  .settings(
    name := "Citrix2Rocket"
  )

libraryDependencies ++= List(
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "io.spray" %% "spray-json" % "1.3.6",
  "org.jsoup" % "jsoup" % "1.15.3",
  "com.outr" %% "scribe" % "3.10.7",
  "org.apache.commons" % "commons-email" % "1.5"

)
