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

import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import uk.gov.hmrc.soletraderidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.soletraderidentificationfrontend.stubs.{AuthStub, SoleTraderIdentificationStub}
import uk.gov.hmrc.soletraderidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.soletraderidentificationfrontend.views.CaptureSautrViewTests

class CaptureSautrControllerISpec extends ComponentSpecHelper
  with CaptureSautrViewTests
  with SoleTraderIdentificationStub
  with AuthStub {

  "GET /sa-utr" should {
    lazy val result = {
      stubAuth(OK, successfulAuthResponse())
      get(s"/identify-your-sole-trader-business/$testJourneyId/sa-utr")
    }

    "return OK" in {
      result.status mustBe OK
    }

    "return a view which" should {
      testCaptureSautrView(result)
    }

    "redirect to sign in page" when {
      "the user is UNAUTHORISED" in {
        stubAuthFailure()
        lazy val result: WSResponse = get(s"/identify-your-sole-trader-business/$testJourneyId/sa-utr")

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri("/bas-gateway/sign-in" +
            s"?continue_url=%2Fidentify-your-sole-trader-business%2F$testJourneyId%2Fsa-utr" +
            "&origin=sole-trader-identification-frontend"
          )
        )
      }
    }
  }

  "POST /sa-utr" when {
    "the sautr is correctly formatted" should {
      "redirect to Check Your Answers Page and store the data in the backend" in {
        stubAuth(OK, successfulAuthResponse())
        stubStoreSautr(testJourneyId, testSautr)(status = OK)

        lazy val result = post(s"/identify-your-sole-trader-business/$testJourneyId/sa-utr")("sa-utr" -> testSautr)

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CheckYourAnswersController.show(testJourneyId).url)
        )
      }
    }

    "no sautr is submitted" should {
      lazy val result = {
        stubAuth(OK, successfulAuthResponse())
        post(s"/identify-your-sole-trader-business/$testJourneyId/sa-utr")("sa-utr" -> "")
      }

      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }

      testCaptureSautrErrorMessages(result)
    }

    "an invalid sautr is submitted" should {
      lazy val result = {
        stubAuth(OK, successfulAuthResponse())
        post(s"/identify-your-sole-trader-business/$testJourneyId/sa-utr")("sa-utr" -> "123456789")
      }

      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }

      testCaptureSautrErrorMessages(result)
    }
  }
}
