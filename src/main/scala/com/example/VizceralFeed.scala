package com.example

import java.time.format.DateTimeFormatter
import java.time.{Clock, LocalDateTime, ZonedDateTime}

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import scala.concurrent.duration._

object VizceralFeedActor {
  def props(profileName: String): Props = {
    val s3 = AmazonS3ClientBuilder
      .standard()
      .withCredentials(new ProfileCredentialsProvider(profileName))
      .withRegion("ap-southeast-1")
      .build()
    props(s3)
  }
  def props(s3: AmazonS3): Props = Props(new VizceralFeedActor(s3))

  final case object ProcessFeedInterval

  def buildGraph(): String = {
    // apSoutheast1 graph
    val apSoutheast1Nodes = List(
      Node("focusedChild", "INTERNET"),
      Node("focusedChild", "elb")
    )
    val apSoutheast1Connections = List(
      Connection("INTERNET", "elb", Metrics(5000))
    )

    // global graph
    val globalNodes = List(
      Node("region", "INTERNET"),
      Node("region", "ap-southeast-1", None, Some(apSoutheast1Nodes), Some(apSoutheast1Connections))
    )
    val globalConnections = List(
      Connection("INTERNET", "ap-southeast-1", Metrics(5000))
    )

    val model = Node("global", "edge", None, Some(globalNodes), Some(globalConnections))

    // Generate json string
    val printer = Printer.spaces2.copy(dropNullKeys = true)
    printer.pretty(model.asJson)
  }
}

class VizceralFeedActor(s3: AmazonS3) extends Actor with ActorLogging {
  import VizceralFeedActor._
  import context.dispatcher

  val bucketName = "vizceraltemplates"

  def receive() = {
    case ProcessFeedInterval => {
      val now = LocalDateTime.now(Clock.systemUTC())

      // load from ES

      // build graph
      val newGraph = buildGraph()

      // push to S3
      val dateString = now.format(DateTimeFormatter.ISO_DATE_TIME)
      s3.putObject(bucketName, "vizgraph.json.latest", newGraph)
      s3.putObject(bucketName, s"vizgraph.json.$dateString", newGraph)

      // Schedule another interval check in 10 second
      context.system.scheduler.scheduleOnce(10 second, self, ProcessFeedInterval)
    }
  }
}

object VizceralFeed extends App {
  import VizceralFeedActor._

  val system: ActorSystem = ActorSystem("vizceralFeed")

  val viz: ActorRef = system.actorOf(VizceralFeedActor.props("devman"), "vizActor")

  viz ! ProcessFeedInterval
}
