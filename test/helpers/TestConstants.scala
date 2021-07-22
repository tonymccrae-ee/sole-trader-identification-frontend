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

import uk.gov.hmrc.soletraderidentificationfrontend.models.EntityType.SoleTrader
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
      testFullName,
      testDateOfBirth,
      testNino,
      Some(testSautr),
      BusinessVerificationPass,
      Registered(testSafeId)
    )

  val testSoleTraderDetailsNoSautr: SoleTraderDetails =
    SoleTraderDetails(
      testFullName,
      testDateOfBirth,
      testNino,
      None,
      BusinessVerificationUnchallenged,
      RegistrationNotCalled
    )

  val testAuthenticatorDetails: AuthenticatorDetails =
    AuthenticatorDetails(
      testFirstName,
      testLastName,
      testDateOfBirth,
      testNino,
      Some(testSautr)
    )

  val testAuthenticatorDetailsNoSautr: AuthenticatorDetails =
    AuthenticatorDetails(
      testFirstName,
      testLastName,
      testDateOfBirth,
      testNino,
      None
    )

  def testSoleTraderJourneyConfig(enableSautrCheck: Boolean = false): JourneyConfig = JourneyConfig(
    continueUrl = testContinueUrl,
    pageConfig = PageConfig(
      optServiceName = None,
      deskProServiceId = "vrs",
      signOutUrl = testSignOutUrl,
      enableSautrCheck = enableSautrCheck
    ),
    entityType = SoleTrader
  )

}
