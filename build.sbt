// The file <project root>/project/Dependencies.scala contains a full list of all the AWS apis you can use in your
// libraryDependencies section below. You can also update the version of the AWS libs in this file as well.
import Dependencies._

// Project definition
lazy val vizceralFeed = project
  .in(file("."))
  .settings(
    inThisBuild(List(
      name := "vizceralFeed",
      organization := "com.example",
      scalaVersion := "2.12.4",
      version      := "0.1.0-SNAPSHOT",
      scalacOptions ++= Seq("-unchecked", "-deprecation"),
    )),
    libraryDependencies ++= Seq(
      akkaActor,
      circeCore,
      circeGeneric,
      circeParser,
      awsS3,
      logback,
      akkaTestKit % Test,
      scalaTest % Test
    )
  )
