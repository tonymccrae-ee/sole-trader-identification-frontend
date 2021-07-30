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

package helpers

import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.soletraderidentificationfrontend.models.EntityType.{EntityType, SoleTrader}
import uk.gov.hmrc.soletraderidentificationfrontend.models.SoleTraderDetailsMatching.Mismatch
import uk.gov.hmrc.soletraderidentificationfrontend.models._

import java.time.LocalDate
import java.util.UUID


object TestConstants {

  val testJourneyId: String = UUID.randomUUID().toString
  val testSafeId: String = UUID.randomUUID().toString
  val testInternalId: String = UUID.randomUUID().toString
  val testSautr: String = "1234567890"
  val testContinueUrl: String = "/test"
  val testSignOutUrl: String = "/sign-out"
  val testDateOfBirth: LocalDate = LocalDate.now().minusYears(17)
  val testFirstName: String = "John"
  val testLastName: String = "Smith"
  val testFullName: FullName = FullName(testFirstName, testLastName)
  val testNino: String = "AA111111A"

  val testSoleTraderDetails: SoleTraderDetails =
    SoleTraderDetails(
      fullName = testFullName,
      dateOfBirth = testDateOfBirth,
      nino = testNino,
      optSautr = Some(testSautr),
      identifiersMatch = true,
      businessVerification = BusinessVerificationPass,
      registrationStatus = Registered(testSafeId)
    )

  val testSoleTraderDetailsNoSautr: SoleTraderDetails =
    SoleTraderDetails(
      fullName = testFullName,
      dateOfBirth = testDateOfBirth,
      nino = testNino,
      optSautr = None,
      identifiersMatch = true,
      businessVerification = BusinessVerificationUnchallenged,
      registrationStatus = RegistrationNotCalled
    )

  val testIndividualDetails: IndividualDetails =
    IndividualDetails(
      firstName = testFirstName,
      lastName = testLastName,
      dateOfBirth = testDateOfBirth,
      nino = testNino,
      optSautr = Some(testSautr)
    )

  val testIndividualDetailsNoSautr: IndividualDetails =
    IndividualDetails(
      firstName = testFirstName,
      lastName = testLastName,
      dateOfBirth = testDateOfBirth,
      nino = testNino,
      optSautr = None
    )

  def testJourneyConfig(enableSautrCheck: Boolean = false, entityType: EntityType = SoleTrader): JourneyConfig = JourneyConfig(
    continueUrl = testContinueUrl,
    pageConfig = PageConfig(
      optServiceName = None,
      deskProServiceId = "vrs",
      signOutUrl = testSignOutUrl,
      enableSautrCheck = enableSautrCheck
    ),
    entityType = entityType
  )

  val testIndividualSuccessfulAuditEventJson: JsObject = Json.obj(
    "firstName" -> testFirstName,
    "lastName" -> testLastName,
    "dateOfBirth" -> testDateOfBirth,
    "nino" -> testNino,
    "identifiersMatch" -> true,
    "authenticatorResponse" -> Json.toJson(testIndividualDetailsNoSautr)
  )


  val testIndividualFailureAuditEventJson: JsObject = Json.obj(
    "firstName" -> testFirstName,
    "lastName" -> testLastName,
    "dateOfBirth" -> testDateOfBirth,
    "nino" -> testNino,
    "identifiersMatch" -> false,
    "authenticatorResponse" -> Mismatch.toString
  )


}
