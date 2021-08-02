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

package uk.gov.hmrc.soletraderidentificationfrontend.controllers

import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsBoolean, Json}
import play.api.test.Helpers._
import uk.gov.hmrc.soletraderidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.soletraderidentificationfrontend.models._
import uk.gov.hmrc.soletraderidentificationfrontend.stubs.{AuthStub, RegisterStub, SoleTraderIdentificationStub}
import uk.gov.hmrc.soletraderidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.soletraderidentificationfrontend.utils.WiremockHelper.{stubAudit, verifyAudit}

class RegistrationControllerISpec extends ComponentSpecHelper with AuthStub with SoleTraderIdentificationStub with RegisterStub {

  def extraConfig = Map(
    "auditing.enabled" -> "true",
    "auditing.consumer.baseUri.host" -> mockHost,
    "auditing.consumer.baseUri.port" -> mockPort
  )

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(config ++ extraConfig)
    .build

  "GET /:journeyId/register" should {
    "redirect to continueUrl" when {
      "registration is successful and registration status is successfully stored" in {
        stubAuth(OK, successfulAuthResponse())
        stubRetrieveNino(testJourneyId)(status = OK, body = testNino)
        stubRetrieveSautr(testJourneyId)(status = OK, body = testSautr)
        stubRetrieveBusinessVerificationStatus(testJourneyId)(status = OK, body = Json.toJson(BusinessVerificationPass))
        stubRegister(testNino, testSautr)(status = OK, body = Registered(testSafeId))
        stubStoreRegistrationStatus(testJourneyId, Registered(testSafeId))(status = OK)
        stubAudit()
        stubRetrieveFullName(testJourneyId)(OK, Json.toJson(FullName(testFirstName, testLastName)))
        stubRetrieveDob(testJourneyId)(OK, Json.toJson(testDateOfBirth))
        stubRetrieveNino(testJourneyId)(OK, testNino)
        stubRetrieveSautr(testJourneyId)(OK, testSautr)
        stubRetrieveIdentifiersMatch(testJourneyId)(OK, JsBoolean(true))
        stubRetrieveAuthenticatorDetails(testJourneyId)(OK, testIndividualDetailsJson)
        stubRetrieveBusinessVerificationStatus(testJourneyId)(OK, Json.toJson(BusinessVerificationPass))
        stubRetrieveRegistrationStatus(testJourneyId)(OK, Json.toJson(Registered(testSafeId)))

        val result = get(s"$baseUrl/$testJourneyId/register")
        result.status mustBe SEE_OTHER
        result.header(LOCATION) mustBe Some(routes.JourneyRedirectController.redirectToContinueUrl(testJourneyId).url)

        verifyRegister(testNino, testSautr)
        verifyStoreRegistrationStatus(testJourneyId, Registered(testSafeId))
        verifyAudit()
      }

      "registration failed and registration status is successfully stored" in {
        stubAuth(OK, successfulAuthResponse())
        stubRetrieveNino(testJourneyId)(status = OK, body = testNino)
        stubRetrieveSautr(testJourneyId)(status = OK, body = testSautr)
        stubRetrieveBusinessVerificationStatus(testJourneyId)(status = OK, body = Json.toJson(BusinessVerificationPass))
        stubRegister(testNino, testSautr)(status = OK, body = RegistrationFailed)
        stubStoreRegistrationStatus(testJourneyId, RegistrationFailed)(status = OK)
        stubAudit()
        stubRetrieveFullName(testJourneyId)(OK, Json.toJson(FullName(testFirstName, testLastName)))
        stubRetrieveDob(testJourneyId)(OK, Json.toJson(testDateOfBirth))
        stubRetrieveNino(testJourneyId)(OK, testNino)
        stubRetrieveSautr(testJourneyId)(OK, testSautr)
        stubRetrieveIdentifiersMatch(testJourneyId)(OK, JsBoolean(true))
        stubRetrieveAuthenticatorDetails(testJourneyId)(OK, testIndividualDetailsJson)
        stubRetrieveBusinessVerificationStatus(testJourneyId)(OK, Json.toJson(BusinessVerificationPass))
        stubRetrieveRegistrationStatus(testJourneyId)(OK, Json.toJson(RegistrationFailed))

        val result = get(s"$baseUrl/$testJourneyId/register")
        result.status mustBe SEE_OTHER
        result.header(LOCATION) mustBe Some(routes.JourneyRedirectController.redirectToContinueUrl(testJourneyId).url)
        verifyRegister(testNino, testSautr)
        verifyStoreRegistrationStatus(testJourneyId, RegistrationFailed)
        verifyAudit()
      }
    }

    "redirect to SignInPage" when {
      "the user is unauthorised" in {
        stubAuthFailure()
        stubAudit()

        val result = get(s"$baseUrl/$testJourneyId/register")
        result.status mustBe SEE_OTHER
        result.header(LOCATION) mustBe Some(s"/bas-gateway/sign-in?continue_url=%2Fidentify-your-sole-trader-business%2F$testJourneyId%2Fregister&origin=sole-trader-identification-frontend")

        verifyAudit()
      }
    }

    "throw an exception" when {
      "business verification is in an invalid state" in {
        stubAuth(OK, successfulAuthResponse())
        stubRetrieveNino(testJourneyId)(status = OK, body = testNino)
        stubRetrieveSautr(testJourneyId)(status = OK, body = testSautr)
        stubRetrieveBusinessVerificationStatus(testJourneyId)(status = BAD_REQUEST)
        stubAudit()

        val result = get(s"$baseUrl/$testJourneyId/register")
        result.status mustBe INTERNAL_SERVER_ERROR

        verifyAudit()
      }

      "nino is missing" in {
        stubAuth(OK, successfulAuthResponse())
        stubRetrieveNino(testJourneyId)(status = NOT_FOUND)
        stubRetrieveSautr(testJourneyId)(status = OK, body = testSautr)
        stubRetrieveBusinessVerificationStatus(testJourneyId)(status = OK, body = Json.toJson(BusinessVerificationPass))
        stubAudit()

        val result = get(s"$baseUrl/$testJourneyId/register")
        result.status mustBe INTERNAL_SERVER_ERROR

        verifyAudit()
      }

      "sautr is missing" in {
        stubAuth(OK, successfulAuthResponse())
        stubRetrieveNino(testJourneyId)(status = OK, body = testNino)
        stubRetrieveSautr(testJourneyId)(status = NOT_FOUND)
        stubRetrieveBusinessVerificationStatus(testJourneyId)(status = OK, body = Json.toJson(BusinessVerificationPass))
        stubAudit()

        val result = get(s"$baseUrl/$testJourneyId/register")
        result.status mustBe INTERNAL_SERVER_ERROR

        verifyAudit()
      }

      "business verification status is missing" in {
        stubAuth(OK, successfulAuthResponse())
        stubRetrieveNino(testJourneyId)(status = OK, body = testNino)
        stubRetrieveSautr(testJourneyId)(status = OK, body = testSautr)
        stubRetrieveBusinessVerificationStatus(testJourneyId)(status = NOT_FOUND)
        stubAudit()

        val result = get(s"$baseUrl/$testJourneyId/register")
        result.status mustBe INTERNAL_SERVER_ERROR

        verifyAudit()
      }
    }
  }
}
