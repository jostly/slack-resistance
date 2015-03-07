scalaVersion in ThisBuild := "2.11.5"

libraryDependencies in ThisBuild ++= {
  val akkaV = "2.3.9"
  val sprayV = "1.3.2"
  Seq(
    "io.spray"            %%  "spray-can"     % sprayV,
    "io.spray"            %%  "spray-routing" % sprayV,
    "io.spray"            %%  "spray-client"  % sprayV,
    "io.spray"            %%  "spray-json"    % "1.3.1",
    "org.json4s"          %%  "json4s-native" % "3.2.11",
    "com.typesafe.akka"   %%  "akka-actor"    % akkaV,
    "com.typesafe"        %   "config"        % "1.2.1",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
    "ch.qos.logback"      % "logback-classic" % "1.1.2",
    "io.spray"            %%  "spray-testkit" % sprayV  % "test",
    "com.typesafe.akka"   %%  "akka-testkit"  % akkaV   % "test",
    "org.scalatest"       %% "scalatest"      % "2.2.4" % "test"
  )
}

herokuAppName in Compile := "dev-slack-resistance"

exportJars := true

enablePlugins(JavaAppPackaging)

import com.typesafe.sbt.SbtNativePackager._
import NativePackagerKeys._


lazy val root = Project(
	id = "slack-resistance",
	base = file("."),
	settings = packageArchetype.java_application++ Seq(
		mainClass in (Compile) := Some("slackres.playcontext.application.Boot")
	), aggregate = Seq(playContext, acceptance)
) dependsOn (playContext)

