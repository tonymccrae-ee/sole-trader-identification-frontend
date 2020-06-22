/*
 * Copyright 2019 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.soletraderidentificationfrontend.utils

import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Matchers}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import org.scalatestplus.play.{MixedPlaySpec, PortNumber}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Writes
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}
import play.api.test.Helpers._
import play.api.{Application, Environment, Mode}

trait ComponentSpecHelper extends MixedPlaySpec with CustomMatchers with WiremockHelper with BeforeAndAfterAll with BeforeAndAfterEach {

  val mockHost: String = WiremockHelper.wiremockHost
  val mockPort: String = WiremockHelper.wiremockPort.toString
  val mockUrl: String = s"http://$mockHost:$mockPort"

  def config: Map[String, String] = Map(
    "auditing.enabled" -> "false",
    "play.filters.csrf.header.bypassHeaders.Csrf-Token" -> "nocheck",
    "microservice.services.auth.host" -> mockHost,
    "microservice.services.auth.port" -> mockPort,
    "microservice.services.base.host" -> mockHost,
    "microservice.services.base.port" -> mockPort,
    "microservice.services.des.url" -> mockUrl
  )

  def app(extraConfig: Map[String, String]): Application = new GuiceApplicationBuilder()
    .in(Environment.simple(mode = Mode.Dev))
    .configure(config ++ extraConfig)
    .build

  def defaultApp: Application = app(Map.empty)

  implicit def ws(implicit app: Application): WSClient = app.injector.instanceOf[WSClient]

  override def beforeAll(): Unit = {
    startWiremock()
    super.beforeAll()
  }

  override def afterAll(): Unit = {
    stopWiremock()
    super.afterAll()
  }

  override def beforeEach(): Unit = {
    resetWiremock()
    super.beforeEach()
  }

  def get[T](uri: String)(implicit ws: WSClient, portNumber: PortNumber): WSResponse = {
    await(buildClient(uri).get)
  }

  def post[T](uri: String)(body: T)(implicit writes: Writes[T], ws: WSClient, portNumber: PortNumber): WSResponse = {
    await(
      buildClient(uri)
        .withHttpHeaders("Content-Type" -> "application/json")
        .post(writes.writes(body).toString())
    )
  }

  def put[T](uri: String)(body: T)(implicit writes: Writes[T], ws: WSClient, portNumber: PortNumber): WSResponse = {
    await(
      buildClient(uri)
        .withHttpHeaders("Content-Type" -> "application/json")
        .put(writes.writes(body).toString())
    )
  }

  val baseUrl: String = "/sole-trader-identification"

  private def buildClient(path: String)(implicit ws: WSClient, portNumber: PortNumber): WSRequest =
    ws.url(s"http://localhost:${portNumber.value}$baseUrl$path").withFollowRedirects(false)

}
