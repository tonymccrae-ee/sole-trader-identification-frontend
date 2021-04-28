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

import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import uk.gov.hmrc.soletraderidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.soletraderidentificationfrontend.models.SoleTraderDetails
import uk.gov.hmrc.soletraderidentificationfrontend.stubs.{AuthStub, AuthenticatorStub, SoleTraderIdentificationStub}
import uk.gov.hmrc.soletraderidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.soletraderidentificationfrontend.views.CheckYourAnswersViewTests


class CheckYourAnswersControllerISpec extends ComponentSpecHelper
  with CheckYourAnswersViewTests
  with SoleTraderIdentificationStub
  with AuthStub
  with AuthenticatorStub {

  "GET /check-your-answers-business" when {
    "the applicant has a nino and an sautr" should {
      lazy val result: WSResponse = {
        await(insertJourneyConfig(
          journeyId = testJourneyId,
          continueUrl = testContinueUrl,
          optServiceName = None,
          deskProServiceId = testDeskProServiceId,
          signOutUrl = testSignOutUrl,
          enableSautrCheck = true
        ))
        stubAuth(OK, successfulAuthResponse())
        stubRetrieveSoleTraderDetails(testJourneyId)(status = OK, body = testSoleTraderDetailsJson)
        get(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")
      }

      "return OK" in {
        result.status mustBe OK
      }

      "return a view which" should {
        testCheckYourAnswersFullView(result, testJourneyId)
      }

      "redirect to sign in page" when {
        "the user is UNAUTHORISED" in {
          stubAuthFailure()
          lazy val result: WSResponse = get(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")

          result must have(
            httpStatus(SEE_OTHER),
            redirectUri("/bas-gateway/sign-in" +
              s"?continue_url=%2Fidentify-your-sole-trader-business%2F$testJourneyId%2Fcheck-your-answers-business" +
              "&origin=sole-trader-identification-frontend"
            )
          )
        }
      }
    }

    "the applicant only has a nino" should {
      lazy val result: WSResponse = {
        await(insertJourneyConfig(
          journeyId = testJourneyId,
          continueUrl = testContinueUrl,
          optServiceName = None,
          deskProServiceId = testDeskProServiceId,
          signOutUrl = testSignOutUrl,
          enableSautrCheck = false
        ))
        stubAuth(OK, successfulAuthResponse())
        stubRetrieveSoleTraderDetails(testJourneyId)(status = OK, body = testSoleTraderDetailsJson)
        get(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")
      }

      "return OK" in {
        result.status mustBe OK
      }

      "return a view which" should {
        testCheckYourAnswersNoSautrView(result, testJourneyId)
      }

      "redirect to sign in page" when {
        "the user is UNAUTHORISED" in {
          stubAuthFailure()
          lazy val result: WSResponse = get(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")

          result must have(
            httpStatus(SEE_OTHER),
            redirectUri("/bas-gateway/sign-in" +
              s"?continue_url=%2Fidentify-your-sole-trader-business%2F$testJourneyId%2Fcheck-your-answers-business" +
              "&origin=sole-trader-identification-frontend"
            )
          )
        }
      }
    }
  }

  "POST /check-your-answers-business" should {
    "redirect to continue url from the supplied journey config" when {
      "the user has only provided a nino and the calling service has not enabled the sautr check" in {
        await(insertJourneyConfig(
          journeyId = testJourneyId,
          continueUrl = testContinueUrl,
          optServiceName = None,
          deskProServiceId = testDeskProServiceId,
          signOutUrl = testSignOutUrl,
          enableSautrCheck = false
        ))
        stubRetrieveSoleTraderDetails(testJourneyId)(status = OK, body = testSoleTraderDetailsNoSautrJson)
        stubAuth(OK, successfulAuthResponse())
        stubMatch(testSoleTraderDetailsNoSautr)(OK, successfulMatchJson(testSoleTraderDetailsNoSautr))


        lazy val result = post(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")()

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(s"$testContinueUrl?journeyId=$testJourneyId")
        )
      }

      "the user has provided both a nino and an sautr" when {
        "the calling service has enabled the sautr check" in {
          await(insertJourneyConfig(
            journeyId = testJourneyId,
            continueUrl = testContinueUrl,
            optServiceName = None,
            deskProServiceId = testDeskProServiceId,
            signOutUrl = testSignOutUrl,
            enableSautrCheck = true
          ))
          stubRetrieveSoleTraderDetails(testJourneyId)(status = OK, body = testSoleTraderDetailsJson)
          stubAuth(OK, successfulAuthResponse())
          stubMatch(testSoleTraderDetails)(OK, successfulMatchJson(testSoleTraderDetails))


          lazy val result = post(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")()

          result must have(
            httpStatus(SEE_OTHER),
            redirectUri(s"$testContinueUrl?journeyId=$testJourneyId")
          )
        }
      }

      "the user has only provided a nino and does not have an sautr" when {
        "the calling service has enabled the sautr check" in {
          await(insertJourneyConfig(
            journeyId = testJourneyId,
            continueUrl = testContinueUrl,
            optServiceName = None,
            deskProServiceId = testDeskProServiceId,
            signOutUrl = testSignOutUrl,
            enableSautrCheck = true
          ))
          stubRetrieveSoleTraderDetails(testJourneyId)(status = OK, body = testSoleTraderDetailsNoSautrJson)
          stubAuth(OK, successfulAuthResponse())
          stubMatch(testSoleTraderDetailsNoSautr)(OK, successfulMatchJson(testSoleTraderDetailsNoSautr))


          lazy val result = post(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")()

          result must have(
            httpStatus(SEE_OTHER),
            redirectUri(s"$testContinueUrl?journeyId=$testJourneyId")
          )
        }
      }
    }

    "redirect to personal information error page" when {
      "the user has provided an sautr that does not exist in authenticator" in {
        await(insertJourneyConfig(
          journeyId = testJourneyId,
          continueUrl = testContinueUrl,
          optServiceName = None,
          deskProServiceId = testDeskProServiceId,
          signOutUrl = testSignOutUrl,
          enableSautrCheck = true
        ))
        stubRetrieveSoleTraderDetails(testJourneyId)(status = OK, body = testSoleTraderDetailsJson)
        stubAuth(OK, successfulAuthResponse())
        stubMatch(testSoleTraderDetails)(OK, successfulMatchJson(testSoleTraderDetailsNoSautr))

        lazy val result = post(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")()

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.PersonalInformationErrorController.show(testJourneyId).url)
        )
      }

      "the user has provided an sautr that does not match what is returned from authenticator" in {
        val invalidSoleTraderDetails: SoleTraderDetails = testSoleTraderDetails.copy(optSautr = Some("0000000000"))
        val invalidSoleTraderDetailsJson = Json.obj("fullName" -> Json.obj(
          "firstName" -> invalidSoleTraderDetails.firstName,
          "lastName" -> invalidSoleTraderDetails.lastName
        ),
          "dateOfBirth" -> invalidSoleTraderDetails.dateOfBirth,
          "nino" -> invalidSoleTraderDetails.nino,
          "sautr" -> "0000000000"
        )

        await(insertJourneyConfig(
          journeyId = testJourneyId,
          continueUrl = testContinueUrl,
          optServiceName = None,
          deskProServiceId = testDeskProServiceId,
          signOutUrl = testSignOutUrl,
          enableSautrCheck = true
        ))
        stubRetrieveSoleTraderDetails(testJourneyId)(status = OK, body = invalidSoleTraderDetailsJson)
        stubAuth(OK, successfulAuthResponse())
        stubMatch(invalidSoleTraderDetails)(OK, successfulMatchJson(testSoleTraderDetails))

        lazy val result = post(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")()

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.PersonalInformationErrorController.show(testJourneyId).url)
        )
      }

      "when authenticator returns a mismatch" in {
        await(insertJourneyConfig(
          journeyId = testJourneyId,
          continueUrl = testContinueUrl,
          optServiceName = None,
          deskProServiceId = testDeskProServiceId,
          signOutUrl = testSignOutUrl,
          enableSautrCheck = false
        ))
        stubRetrieveSoleTraderDetails(testJourneyId)(status = OK, body = testSoleTraderDetailsJson)
        stubAuth(OK, successfulAuthResponse())
        stubMatch(testSoleTraderDetails)(UNAUTHORIZED, mismatchErrorJson)
        lazy val result = post(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")()

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.PersonalInformationErrorController.show(testJourneyId).url)
        )
      }
    }
  }

}
