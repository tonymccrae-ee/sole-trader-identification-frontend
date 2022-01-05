/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.soletraderidentificationfrontend.stubs

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.soletraderidentificationfrontend.utils.{WireMockMethods, WiremockHelper}

trait RegisterStub extends WireMockMethods {

  def stubRegister(nino: String, sautr: String)(status: Int, body: JsObject): StubMapping = {
    val jsonBody = Json.obj("soleTrader" ->
      Json.obj(
        "nino" -> nino,
        "sautr" -> sautr)
    )
    when(method = POST, uri = "/sole-trader-identification/register", jsonBody)
      .thenReturn(
        status = status,
        body = Json.obj("registration" -> body)
      )
  }

  def stubRegisterWithTrn(trn: String, sautr: String)(status: Int, body: JsObject): StubMapping = {
    val jsonBody = Json.obj(
      "trn" -> trn,
      "sautr" -> sautr
    )

    when(method = POST, uri = "/sole-trader-identification/register-trn", jsonBody)
      .thenReturn(
        status = status,
        body = Json.obj("registration" -> body)
      )
  }

  def verifyRegister(nino: String, sautr: String): Unit = {
    val jsonBody = Json.obj(
      "soleTrader" -> Json.obj(
        "nino" -> nino,
        "sautr" -> sautr
      )
    )
    WiremockHelper.verifyPost(uri = "/sole-trader-identification/register", optBody = Some(jsonBody.toString()))
  }

  def verifyRegisterWithTrn(trn: String, sautr: String): Unit = {
    val jsonBody = Json.obj(
      "trn" -> trn,
      "sautr" -> sautr
    )

    WiremockHelper.verifyPost(uri = "/sole-trader-identification/register-trn", optBody = Some(jsonBody.toString()))

  }
}
