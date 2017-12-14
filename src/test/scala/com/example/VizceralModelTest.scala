package com.example

import org.scalatest.{FlatSpec, Matchers}
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._

class VizceralModelTest extends FlatSpec with Matchers {

  "The following" should "generate the minimum viable global graph" in {

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

    val printer = Printer.spaces2.copy(dropNullKeys = true)
    println(printer.pretty(model.asJson))
  }

//  it should "throw NoSuchElementException if an empty stack is popped" in {
//    false
//  }
}
