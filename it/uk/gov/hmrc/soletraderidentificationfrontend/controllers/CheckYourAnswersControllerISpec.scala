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

import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import uk.gov.hmrc.soletraderidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.soletraderidentificationfrontend.models.{BusinessVerificationUnchallenged, RegistrationNotCalled}
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
          internalId = testInternalId,
          continueUrl = testContinueUrl,
          optServiceName = None,
          deskProServiceId = testDeskProServiceId,
          signOutUrl = testSignOutUrl,
          enableSautrCheck = true
        ))
        stubAuth(OK, successfulAuthResponse())
        stubRetrieveAuthenticatorDetails(testJourneyId)(OK, testIndividualDetailsJson)
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
          internalId = testInternalId,
          continueUrl = testContinueUrl,
          optServiceName = None,
          deskProServiceId = testDeskProServiceId,
          signOutUrl = testSignOutUrl,
          enableSautrCheck = false
        ))
        stubAuth(OK, successfulAuthResponse())
        stubRetrieveAuthenticatorDetails(testJourneyId)(OK, testIndividualDetailsJsonNoSautr)
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
    "redirect to the journey redirect controller" when {
      "the user has only provided a nino and the calling service has not enabled the sautr check" in {
        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          continueUrl = testContinueUrl,
          optServiceName = None,
          deskProServiceId = testDeskProServiceId,
          signOutUrl = testSignOutUrl,
          enableSautrCheck = false
        ))
        stubRetrieveAuthenticatorDetails(testJourneyId)(OK, testIndividualDetailsJsonNoSautr)
        stubAuth(OK, successfulAuthResponse())
        stubMatch(testIndividualDetailsNoSautr)(OK, successfulMatchJson(testIndividualDetailsNoSautr))
        stubStoreIdentifiersMatch(testJourneyId, identifiersMatch = true)(OK)
        stubStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationUnchallenged)(OK)
        stubStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)(OK)

        lazy val result = post(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")()

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.JourneyRedirectController.redirectToContinueUrl(testJourneyId).url)
        )

        verifyStoreIdentifiersMatch(testJourneyId, identifiersMatch = true)
      }

      "the user has only provided a nino and does not have an sautr that matches what is held in the database" when {
        "the calling service has enabled the sautr check" in {
          await(insertJourneyConfig(
            journeyId = testJourneyId,
            internalId = testInternalId,
            continueUrl = testContinueUrl,
            optServiceName = None,
            deskProServiceId = testDeskProServiceId,
            signOutUrl = testSignOutUrl,
            enableSautrCheck = true
          ))
          stubRetrieveAuthenticatorDetails(testJourneyId)(OK, testIndividualDetailsJsonNoSautr)
          stubAuth(OK, successfulAuthResponse())
          stubMatch(testIndividualDetailsNoSautr)(OK, successfulMatchJson(testIndividualDetailsNoSautr))
          stubStoreIdentifiersMatch(testJourneyId, identifiersMatch = true)(OK)
          stubRetrieveSautr(testJourneyId)(NOT_FOUND)
          stubStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationUnchallenged)(OK)
          stubStoreRegistrationStatus(testJourneyId, RegistrationNotCalled)(OK)

          lazy val result = post(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")()

          result must have(
            httpStatus(SEE_OTHER),
            redirectUri(routes.JourneyRedirectController.redirectToContinueUrl(testJourneyId).url)
          )

          verifyStoreIdentifiersMatch(testJourneyId, identifiersMatch = true)
        }
      }
    }

    "redirect to start business verification" when {
      "the user has provided both a nino and an sautr that match what is held in the database" when {
        "the calling service has enabled the sautr check" in {
          await(insertJourneyConfig(
            journeyId = testJourneyId,
            internalId = testInternalId,
            continueUrl = testContinueUrl,
            optServiceName = None,
            deskProServiceId = testDeskProServiceId,
            signOutUrl = testSignOutUrl,
            enableSautrCheck = true
          ))
          stubRetrieveAuthenticatorDetails(testJourneyId)(OK, testIndividualDetailsJson)
          stubAuth(OK, successfulAuthResponse())
          stubMatch(testIndividualDetails)(OK, successfulMatchJson(testIndividualDetails))
          stubStoreIdentifiersMatch(testJourneyId, identifiersMatch = true)(OK)
          stubRetrieveSautr(testJourneyId)(OK, testSautr)

          lazy val result = post(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")()

          result must have(
            httpStatus(SEE_OTHER),
            redirectUri(routes.BusinessVerificationController.startBusinessVerificationJourney(testJourneyId).url)
          )

          verifyStoreIdentifiersMatch(testJourneyId, identifiersMatch = true)
        }
      }
    }

    "redirect to personal information error page" when {
      "the user has provided an sautr that does not exist in authenticator" in {
        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          continueUrl = testContinueUrl,
          optServiceName = None,
          deskProServiceId = testDeskProServiceId,
          signOutUrl = testSignOutUrl,
          enableSautrCheck = true
        ))
        stubRetrieveAuthenticatorDetails(testJourneyId)(OK, testIndividualDetailsJson)
        stubAuth(OK, successfulAuthResponse())
        stubMatch(testIndividualDetails)(OK, successfulMatchJson(testIndividualDetailsNoSautr))
        stubStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)(OK)

        lazy val result = post(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")()

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.PersonalInformationErrorController.show(testJourneyId).url)
        )

        verifyStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)
      }

      "the user has provided an sautr that does not match what is returned from authenticator" in {
        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          continueUrl = testContinueUrl,
          optServiceName = None,
          deskProServiceId = testDeskProServiceId,
          signOutUrl = testSignOutUrl,
          enableSautrCheck = true
        ))

        val testAuthenticatorDetailsJson: JsObject = {
          Json.obj("fullName" -> Json.obj(
            "firstName" -> testFirstName,
            "lastName" -> testLastName
          ),
            "dateOfBirth" -> testDateOfBirth,
            "nino" -> testNino,
            "sautr" -> "0000000000"
          )
        }
        stubRetrieveAuthenticatorDetails(testJourneyId)(OK, testAuthenticatorDetailsJson)
        stubAuth(OK, successfulAuthResponse())
        stubMatch(testIndividualDetails)(UNAUTHORIZED, mismatchErrorJson)
        stubStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)(OK)


        lazy val result = post(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")()

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.PersonalInformationErrorController.show(testJourneyId).url)
        )

        verifyStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)
      }

      "when authenticator returns a mismatch" in {
        await(insertJourneyConfig(
          journeyId = testJourneyId,
          internalId = testInternalId,
          continueUrl = testContinueUrl,
          optServiceName = None,
          deskProServiceId = testDeskProServiceId,
          signOutUrl = testSignOutUrl,
          enableSautrCheck = false
        ))
        stubRetrieveAuthenticatorDetails(testJourneyId)(OK, testIndividualDetailsJson)
        stubAuth(OK, successfulAuthResponse())
        stubMatch(testIndividualDetails)(UNAUTHORIZED, mismatchErrorJson)
        stubStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)(OK)

        lazy val result = post(s"/identify-your-sole-trader-business/$testJourneyId/check-your-answers-business")()

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.PersonalInformationErrorController.show(testJourneyId).url)
        )

        verifyStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)
      }
    }
  }

}
