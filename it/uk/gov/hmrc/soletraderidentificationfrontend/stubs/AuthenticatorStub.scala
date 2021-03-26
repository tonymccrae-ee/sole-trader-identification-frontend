/*
 * Copyright 2020 HM Revenue & Customs
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

import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.soletraderidentificationfrontend.models.SoleTraderDetails
import uk.gov.hmrc.soletraderidentificationfrontend.utils.WireMockMethods

import java.time.format.DateTimeFormatter.ofPattern

trait AuthenticatorStub extends WireMockMethods {
  def stubMatch(soleTraderDetails: SoleTraderDetails)(status: Int, body: JsObject): Unit = {
    when(method = POST,
      uri = s"/authenticator/match",
      body = Json.obj(
        "firstName" -> soleTraderDetails.firstName,
        "lastName" -> soleTraderDetails.lastName,
        "dateOfBirth" -> soleTraderDetails.dateOfBirth.format(ofPattern("yyyy-MM-DD")),
        "nino" -> soleTraderDetails.nino
      )
    ).thenReturn(
      status = status,
      body = body
    )
  }

  def stubMatchStub(soleTraderDetails: SoleTraderDetails)(status: Int, body: JsObject): Unit = {
    when(method = POST,
      uri = s"/identify-your-sole-trader-business/test-only/authenticator/match",
      body = Json.obj(
        "firstName" -> soleTraderDetails.firstName,
        "lastName" -> soleTraderDetails.lastName,
        "dateOfBirth" -> soleTraderDetails.dateOfBirth.format(ofPattern("yyyy-MM-DD")),
        "nino" -> soleTraderDetails.nino
      )
    ).thenReturn(
      status = status,
      body = body
    )
  }

  def successfulMatchJson(soleTraderDetails: SoleTraderDetails): JsObject = Json.obj(
    "firstName" -> soleTraderDetails.firstName,
    "lastName" -> soleTraderDetails.lastName,
    "dateOfBirth" -> soleTraderDetails.dateOfBirth.format(ofPattern("yyyy-MM-DD")),
    "nino" -> soleTraderDetails.nino
  )

  val mismatchErrorJson: JsObject = Json.obj(
    "errors" -> Json.arr("no_match_dob")
  )

  val notFoundErrorJson: JsObject = Json.obj(
    "errors" -> "CID returned no record"
  )
}
