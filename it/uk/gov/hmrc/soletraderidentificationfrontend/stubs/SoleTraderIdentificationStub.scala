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
import play.api.libs.json.{JsString, JsValue, Json}
import uk.gov.hmrc.soletraderidentificationfrontend.models.FullName
import uk.gov.hmrc.soletraderidentificationfrontend.utils.WireMockMethods

import java.time.LocalDate

trait SoleTraderIdentificationStub extends WireMockMethods {

  def stubStoreFullName(journeyId: String, fullName: FullName)(status: Int): StubMapping =
    when(method = PUT,
      uri = s"/sole-trader-identification/journey/$journeyId/fullName",
      body = Json.obj(
        "firstName" -> fullName.firstName,
        "lastName" -> fullName.lastName
      )
    ).thenReturn(
      status = status
    )

  def stubStoreNino(journeyId: String, nino: String)(status: Int): StubMapping =
    when(method = PUT,
      uri = s"/sole-trader-identification/journey/$journeyId/nino",
      body = JsString(nino)
    ).thenReturn(
      status = status
    )

  def stubStoreSautr(journeyId: String, sautr: String)(status: Int): StubMapping =
    when(method = PUT,
      uri = s"/sole-trader-identification/journey/$journeyId/sautr", body = JsString(sautr)
    ).thenReturn(
      status = status
    )

  def stubStoreDob(journeyId: String, dateOfBirth: LocalDate)(status: Int): StubMapping =
    when(method = PUT,
      uri = s"/sole-trader-identification/journey/$journeyId/dateOfBirth", body = Json.toJson(dateOfBirth)
    ).thenReturn(
      status = status
    )


  def stubRetrieveSoleTraderDetails(journeyId: String)(status: Int, body: JsValue = Json.obj()): StubMapping =
    when(method = GET,
      uri = s"/sole-trader-identification/journey/$journeyId"
    ).thenReturn(
      status = status,
      body = body
    )

  def stubRetrieveFullName(journeyId: String)(status: Int, body: JsValue = Json.obj()): StubMapping =
    when(method = GET,
      uri = s"/sole-trader-identification/journey/$journeyId/fullName"
    ).thenReturn(
      status = status,
      body = body
    )

  def stubRetrieveDob(journeyId: String)(status: Int, body: JsValue = Json.obj()): StubMapping =
    when(method = GET,
      uri = s"/sole-trader-identification/journey/$journeyId/dateOfBirth"
    ).thenReturn(
      status = status,
      body = body
    )

  def stubRetrieveNino(journeyId: String)(status: Int, body: String = ""): StubMapping =
    when(method = GET,
      uri = s"/sole-trader-identification/journey/$journeyId/nino"
    ).thenReturn(
      status = status,
      body = JsString(body)
    )

  def stubRetrieveSautr(journeyId: String)(status: Int, body: String = ""): StubMapping =
    when(method = GET,
      uri = s"/sole-trader-identification/journey/$journeyId/sautr"
    ).thenReturn(
      status = status,
      body = JsString(body)
    )

  def stubRemoveSautr(journeyId: String)(status: Int, body: String = ""): StubMapping =
    when(method = DELETE,
      uri = s"/sole-trader-identification/journey/$journeyId/sautr"
    ).thenReturn(
      status = status,
      body = body
    )

}