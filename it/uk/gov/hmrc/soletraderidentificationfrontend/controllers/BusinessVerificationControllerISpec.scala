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

import org.scalatest.BeforeAndAfterEach
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.soletraderidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.soletraderidentificationfrontend.featureswitch.core.config.{BusinessVerificationStub, FeatureSwitching}
import uk.gov.hmrc.soletraderidentificationfrontend.models.{BusinessVerificationFail, BusinessVerificationPass, BusinessVerificationUnchallenged}
import uk.gov.hmrc.soletraderidentificationfrontend.stubs.{AuthStub, BusinessVerificationStub, SoleTraderIdentificationStub}
import uk.gov.hmrc.soletraderidentificationfrontend.utils.ComponentSpecHelper

import scala.concurrent.ExecutionContext.Implicits.global

class BusinessVerificationControllerISpec extends ComponentSpecHelper with FeatureSwitching with AuthStub with BusinessVerificationStub
  with SoleTraderIdentificationStub with BeforeAndAfterEach {

  override def beforeEach(): Unit = {
    super.beforeEach()
    disable(BusinessVerificationStub)
    journeyConfigRepository.drop
  }

  "GET /business-verification-result" when {
    s"the $BusinessVerificationStub feature switch is enabled" should {
      "redirect to the continue url if BV status is stored successfully" in {
        await(insertJourneyConfig(
          journeyId = testJourneyId,
          continueUrl = testContinueUrl,
          optServiceName = None,
          deskProServiceId = testDeskProServiceId,
          signOutUrl = testSignOutUrl,
          enableSautrCheck = true
        ))
        enable(BusinessVerificationStub)
        stubAuth(OK, successfulAuthResponse())
        stubRetrieveBusinessVerificationResultFromStub(testBusinessVerificationJourneyId)(OK, Json.obj("verificationStatus" -> "PASS"))
        stubStoreBusinessVerificationStatus(journeyId = testJourneyId, businessVerificationStatus = BusinessVerificationPass)(status = OK)

        lazy val result = get(s"$baseUrl/$testJourneyId/business-verification-result" + s"?journeyId=$testBusinessVerificationJourneyId")

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(s"$testContinueUrl?journeyId=$testJourneyId"))
        verifyStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationPass)
      }

      "throw an exception when the query string is missing" in {
        enable(BusinessVerificationStub)
        stubAuth(OK, successfulAuthResponse())
        stubRetrieveBusinessVerificationResultFromStub(testBusinessVerificationJourneyId)(OK, Json.obj("verificationStatus" -> "PASS"))
        await(insertJourneyConfig(
          journeyId = testJourneyId,
          continueUrl = testContinueUrl,
          optServiceName = None,
          deskProServiceId = testDeskProServiceId,
          signOutUrl = testSignOutUrl,
          enableSautrCheck = true
        ))

        lazy val result = get(s"$baseUrl/$testJourneyId/business-verification-result")

        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }

    s"the $BusinessVerificationStub feature switch is disabled" should {
      "redirect to the continue url if BV status is stored successfully" in {
        await(insertJourneyConfig(
          journeyId = testJourneyId,
          continueUrl = testContinueUrl,
          optServiceName = None,
          deskProServiceId = testDeskProServiceId,
          signOutUrl = testSignOutUrl,
          enableSautrCheck = true
        ))

        stubAuth(OK, successfulAuthResponse())
        stubRetrieveBusinessVerificationResult(testBusinessVerificationJourneyId)(OK, Json.obj("verificationStatus" -> "PASS"))
        stubStoreBusinessVerificationStatus(journeyId = testJourneyId, businessVerificationStatus = BusinessVerificationPass)(status = OK)

        lazy val result = get(s"$baseUrl/$testJourneyId/business-verification-result" + s"?journeyId=$testBusinessVerificationJourneyId")

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(s"$testContinueUrl?journeyId=$testJourneyId"))
        verifyStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationPass)
      }

      "throw an exception when the query string is missing" in {
        stubAuth(OK, successfulAuthResponse())
        stubRetrieveBusinessVerificationResult(testBusinessVerificationJourneyId)(OK, Json.obj("verificationStatus" -> "PASS"))

        lazy val result = get(s"$baseUrl/$testJourneyId/business-verification-result")

        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

  "GET /:journeyId/start-business-verification" when {
    s"the $BusinessVerificationStub feature switch is enabled" should {
      "redirect to business verification redirectUri" when {
        "business verification returns a journey to redirect to" in {
          enable(BusinessVerificationStub)
          stubAuth(OK, successfulAuthResponse())
          stubRetrieveSautr(testJourneyId)(OK, testSautr)
          stubCreateBusinessVerificationJourneyFromStub(testSautr, testJourneyId)(CREATED, Json.obj("redirectUri" -> testContinueUrl))

          lazy val result = get(s"$baseUrl/$testJourneyId/start-business-verification")

          result.status mustBe SEE_OTHER
          result.header(LOCATION) mustBe Some(testContinueUrl)
        }
      }

      "store a verification state of UNCHALLENGED and redirect to the continue url" when {
        "business verification does not have enough information to create a verification journey" in {
          await(insertJourneyConfig(
            journeyId = testJourneyId,
            continueUrl = testContinueUrl,
            optServiceName = None,
            deskProServiceId = testDeskProServiceId,
            signOutUrl = testSignOutUrl,
            enableSautrCheck = true
          ))
          enable(BusinessVerificationStub)
          stubAuth(OK, successfulAuthResponse())
          stubRetrieveSautr(testJourneyId)(OK, testSautr)
          stubCreateBusinessVerificationJourneyFromStub(testSautr, testJourneyId)(NOT_FOUND)
          stubStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationUnchallenged)(OK)

          lazy val result = get(s"$baseUrl/$testJourneyId/start-business-verification")

          result must have(
            httpStatus(SEE_OTHER),
            redirectUri(s"$testContinueUrl?journeyId=$testJourneyId"))
          verifyStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationUnchallenged)
        }
      }
      "store a verification state of FAIL and redirect to the continue url" when {
        "business verification reports the user is locked out" in {
          await(insertJourneyConfig(
            journeyId = testJourneyId,
            continueUrl = testContinueUrl,
            optServiceName = None,
            deskProServiceId = testDeskProServiceId,
            signOutUrl = testSignOutUrl,
            enableSautrCheck = true
          ))
          enable(BusinessVerificationStub)
          stubAuth(OK, successfulAuthResponse())
          stubRetrieveSautr(testJourneyId)(OK, testSautr)
          stubCreateBusinessVerificationJourneyFromStub(testSautr, testJourneyId)(FORBIDDEN)
          stubStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationFail)(OK)

          lazy val result = get(s"$baseUrl/$testJourneyId/start-business-verification")

          result must have(
            httpStatus(SEE_OTHER),
            redirectUri(s"$testContinueUrl?journeyId=$testJourneyId"))
          verifyStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationFail)
        }
      }
    }

    s"the $BusinessVerificationStub feature switch is disabled" should {
      "redirect to business verification redirectUri" when {
        "business verification returns a journey to redirect to" in {
          stubAuth(OK, successfulAuthResponse())
          stubRetrieveSautr(testJourneyId)(OK, testSautr)
          stubCreateBusinessVerificationJourney(testSautr, testJourneyId)(CREATED, Json.obj("redirectUri" -> testContinueUrl))

          lazy val result = get(s"$baseUrl/$testJourneyId/start-business-verification")

          result.status mustBe SEE_OTHER
          result.header(LOCATION) mustBe Some(testContinueUrl)
        }
      }

      "store a verification state of UNCHALLENGED and redirect to the registration controller" when {
        "business verification does not have enough information to create a verification journey" in {
          await(insertJourneyConfig(
            journeyId = testJourneyId,
            continueUrl = testContinueUrl,
            optServiceName = None,
            deskProServiceId = testDeskProServiceId,
            signOutUrl = testSignOutUrl,
            enableSautrCheck = true
          ))
          stubAuth(OK, successfulAuthResponse())
          stubRetrieveSautr(testJourneyId)(OK, testSautr)
          stubCreateBusinessVerificationJourney(testSautr, testJourneyId)(NOT_FOUND)
          stubStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationUnchallenged)(OK)

          lazy val result = get(s"$baseUrl/$testJourneyId/start-business-verification")

          result must have(
            httpStatus(SEE_OTHER),
            redirectUri(s"$testContinueUrl?journeyId=$testJourneyId"))
          verifyStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationUnchallenged)
        }
      }
      "store a verification state of FAIL and redirect to the registration controller" when {
        "business verification reports the user is locked out" in {
          await(insertJourneyConfig(
            journeyId = testJourneyId,
            continueUrl = testContinueUrl,
            optServiceName = None,
            deskProServiceId = testDeskProServiceId,
            signOutUrl = testSignOutUrl,
            enableSautrCheck = true
          ))
          stubAuth(OK, successfulAuthResponse())
          stubRetrieveSautr(testJourneyId)(OK, testSautr)
          stubCreateBusinessVerificationJourney(testSautr, testJourneyId)(FORBIDDEN)
          stubStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationFail)(OK)

          lazy val result = get(s"$baseUrl/$testJourneyId/start-business-verification")

          result must have(
            httpStatus(SEE_OTHER),
            redirectUri(s"$testContinueUrl?journeyId=$testJourneyId"))
          verifyStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationFail)
        }
      }
    }
  }
}
