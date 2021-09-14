name := "facefinish"

version := "0.1"

scalaVersion := "3.0.2"

scalacOptions ++= Seq(
  "-encoding", "UTF-8",
  "-language:implicitConversions",
  "-deprecation",
  "-feature",
  "-unchecked"
)

val AkkaVersion = "2.6.16"
val AkkaHttpVersion = "10.2.6"
val CirceVersion = "0.15.0-M1"
val Selenium = "4.0.0-rc-1"
val SeleniumChromeDriver = "4.0.0-rc-1"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % AkkaVersion,
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,

  // Configuration
  // "com.typesafe" % "config" % "1.4.1",
  "com.github.pureconfig" %% "pureconfig" % "0.16.0",

  // Logging

  // Testing
  "com.typesafe.akka" %% "akka-testkit" % AkkaVersion % Test
).map(_.cross(CrossVersion.for3Use2_13)) ++ Seq(
  // Cats
  "org.typelevel" %% "cats-core" % "2.6.1",
  "org.typelevel" %% "cats-effect" % "3.2.8",

  // CLI
  "com.monovore" %% "decline" % "2.1.0",

  // STTP
  "com.softwaremill.sttp.client3" %% "core" % "3.3.14",
  "com.softwaremill.sttp.client3" %% "circe" % "3.3.14",

  // Logging
  "ch.qos.logback" % "logback-classic" % "1.2.6",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4",

  // Selenium
  "org.seleniumhq.selenium" % "selenium-java" % Selenium,
  "org.seleniumhq.selenium" % "selenium-chrome-driver" % SeleniumChromeDriver,
) ++ Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % CirceVersion)
run / fork := true
