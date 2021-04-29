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

package uk.gov.hmrc.soletraderidentificationfrontend.assets

import java.time.LocalDate
import java.util.UUID

import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.soletraderidentificationfrontend.models.SoleTraderDetails

object TestConstants {

  val testJourneyId: String = UUID.randomUUID().toString
  val testDateOfBirth: LocalDate = LocalDate.now().minusYears(17)
  val testFirstName: String = "John"
  val testLastName: String = "Smith"
  val testNino: String = "AA111111A"
  val testSautr: String = "1234567890"
  val testContinueUrl = "/test-continue-url"

  val testSoleTraderDetails: SoleTraderDetails =
    SoleTraderDetails(
      testFirstName,
      testLastName,
      testDateOfBirth,
      testNino,
      Some(testSautr)
    )

  val testSoleTraderDetailsNoSautr: SoleTraderDetails =
    SoleTraderDetails(
      testFirstName,
      testLastName,
      testDateOfBirth,
      testNino,
      None
    )

  val testSoleTraderDetailsJson: JsObject =
    Json.obj("fullName" -> Json.obj(
      "firstName" -> testFirstName,
      "lastName" -> testLastName
    ),
      "dateOfBirth" -> testDateOfBirth,
      "nino" -> testNino,
      "sautr" -> testSautr
    )

  val testSoleTraderDetailsNoSautrJson: JsObject =
    Json.obj("fullName" -> Json.obj(
      "firstName" -> testFirstName,
      "lastName" -> testLastName
    ),
      "dateOfBirth" -> testDateOfBirth,
      "nino" -> testNino
    )

  val testCredentialId: String = UUID.randomUUID().toString
  val testGGProviderId: String = UUID.randomUUID().toString
  val testGroupId: String = UUID.randomUUID().toString
  val testInternalId: String = UUID.randomUUID().toString

  val testDeskProServiceId: String = "vrs"
  val testSignOutUrl: String = "/sign-out"

}
