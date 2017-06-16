package org.http4s
package client

import java.util.concurrent.Executors

import org.http4s.Http4sSpec
import org.http4s.scalaxml
import org.http4s.Status.Ok
import org.http4s.Method.GET

import scala.xml.Elem

import scalaz.concurrent.Task

class ClientXmlSpec extends Http4sSpec {
  implicit val decoder = scalaxml.xml
  val body = <html><h1>h1</h1></html>
  val xml = s"""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>$body"""
  val service = HttpService {
    case r =>
      Response(Ok).withBody(xml)
  }
  val client = Client.fromHttpService(service)

  "mock client" should {
    "read body before dispose" in {
      client.expect[Elem](Request(GET)).unsafePerformSync must_== body
    }
    "read body in parallel" in {
      val pool = Executors.newFixedThreadPool(5)
      val resp = Task.gatherUnordered((0 to 5).map(_ => Task.fork(client.expect[Elem](Request(GET)))(pool))).unsafePerformSync
      resp.map(_ must_== body)
    }
  }
}
