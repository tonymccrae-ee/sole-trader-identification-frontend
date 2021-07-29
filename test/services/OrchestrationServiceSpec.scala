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

package services

import helpers.TestConstants._
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.test.Helpers._
import services.mocks.{MockAuditService, MockAuthenticatorService, MockJourneyService, MockSoleTraderIdentificationService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.soletraderidentificationfrontend.httpParsers.SoleTraderIdentificationStorageHttpParser.SuccessfullyStored
import uk.gov.hmrc.soletraderidentificationfrontend.models.SoleTraderDetailsMatching.{Matched, Mismatch}
import uk.gov.hmrc.soletraderidentificationfrontend.models._
import uk.gov.hmrc.soletraderidentificationfrontend.services.OrchestrationService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class OrchestrationServiceSpec extends AnyWordSpec
  with Matchers
  with MockJourneyService
  with MockAuthenticatorService
  with MockSoleTraderIdentificationService
  with MockAuditService {

  object TestService extends OrchestrationService(mockJourneyService, mockAuthenticatorService, mockSoleTraderIdentificationService, mockAuditService)

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "orchestrate" should {
    "return SautrMatched" when {
      "the enable sautr check is enabled and the users details match those in the database" in {
        mockGetJourneyConfig(testJourneyId)(Future.successful(testJourneyConfig(enableSautrCheck = true)))
        mockMatchSoleTraderDetails(testJourneyId,testIndividualDetails, testJourneyConfig(enableSautrCheck = true))(Future.successful(Right(Matched)))
        mockStoreIdentifiersMatch(testJourneyId, identifiersMatch = true)(Future.successful(SuccessfullyStored))

        val result = await(TestService.orchestrate(testJourneyId, testIndividualDetails))

        result mustBe SautrMatched

        verifyStoreIdentifiersMatch(testJourneyId, identifiersMatch = true)
      }
    }

    "return NoSautrProvided" when {
      "the enable sautr check is disabled and the users details match those in the database" in {
        mockGetJourneyConfig(testJourneyId)(Future.successful(testJourneyConfig()))
        mockMatchSoleTraderDetails(testJourneyId,testIndividualDetailsNoSautr, testJourneyConfig())(Future.successful(Right(Matched)))
        mockStoreIdentifiersMatch(testJourneyId, identifiersMatch = true)(Future.successful(SuccessfullyStored))
        mockStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationUnchallenged)(Future.successful(SuccessfullyStored))
        mockStoreRegistrationResponse(testJourneyId, RegistrationNotCalled)(Future.successful(SuccessfullyStored))

        val result = await(TestService.orchestrate(testJourneyId, testIndividualDetailsNoSautr))

        result mustBe NoSautrProvided

        verifyStoreIdentifiersMatch(testJourneyId, identifiersMatch = true)
        verifyStoreBusinessVerificationStatus(testJourneyId, BusinessVerificationUnchallenged)
        verifyStoreRegistrationResponse(testJourneyId, RegistrationNotCalled)
      }
    }

    "return DetailsMismatch" when {
      "the enable sautr check is disabled and the details the user provided do not match those in the database" in {
        mockGetJourneyConfig(testJourneyId)(Future.successful(testJourneyConfig()))
        mockMatchSoleTraderDetails(testJourneyId,testIndividualDetailsNoSautr, testJourneyConfig())(Future.successful(Left(Mismatch)))
        mockStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)(Future.successful(SuccessfullyStored))

        val result = await(TestService.orchestrate(testJourneyId, testIndividualDetailsNoSautr))

        result mustBe DetailsMismatch

        verifyStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)
      }
    }

    "return DetailsMismatch" when {
      "the enable sautr check is enabled and the details the user provided do not match those in the database" in {
        mockGetJourneyConfig(testJourneyId)(Future.successful(testJourneyConfig(enableSautrCheck = true)))
        mockMatchSoleTraderDetails(testJourneyId,testIndividualDetailsNoSautr, testJourneyConfig(enableSautrCheck = true))(Future.successful(Left(Mismatch)))
        mockStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)(Future.successful(SuccessfullyStored))

        val result = await(TestService.orchestrate(testJourneyId, testIndividualDetailsNoSautr))

        result mustBe DetailsMismatch

        verifyStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)
      }
    }
  }

}
