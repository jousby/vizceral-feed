package com.example

import java.time.LocalDateTime

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._

object VizceralFeedActor {
  def props(profileName: String): Props = {
    val s3 = AmazonS3ClientBuilder
      .standard()
      .withCredentials(new ProfileCredentialsProvider(profileName))
      .build()
    props(s3)
  }
  def props(s3: AmazonS3): Props = Props(new VizceralFeedActor(s3))

  final case class ProcessFeedInterval(interval: Int)

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

  def receive() = {
    case ProcessFeedInterval(interval) => {
      // load from ES
      // build graph
      val newGraph = buildGraph()
      println(s"New graph has been built: $newGraph")

      // push to S3
      // schedule next
    }
  }
}

object VizceralFeed extends App {
  import VizceralFeedActor._

  val system: ActorSystem = ActorSystem("vizceralFeed")

  val viz: ActorRef = system.actorOf(VizceralFeedActor.props("devman"), "vizActor")

  viz ! ProcessFeedInterval(1)
}
