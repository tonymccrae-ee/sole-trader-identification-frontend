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

import connectors.mocks.MockRetrieveKnownFactsConnector
import helpers.TestConstants.{testJourneyId, testNino, testSaPostcode, testSautr}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import services.mocks.MockSoleTraderIdentificationService
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.soletraderidentificationfrontend.httpParsers.SoleTraderIdentificationStorageHttpParser.SuccessfullyStored
import uk.gov.hmrc.soletraderidentificationfrontend.models.KnownFacts
import uk.gov.hmrc.soletraderidentificationfrontend.services.KnownFactsService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class KnownFactsServiceSpec extends AnyWordSpec with Matchers with MockSoleTraderIdentificationService with MockRetrieveKnownFactsConnector {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  object TestService extends KnownFactsService(mockSoleTraderIdentificationService, mockRetrieveKnownFactsConnector)

  "matchKnownFacts" should {
    "return true" when {
      "the user's postcode matches the postcode returned from ES20" when {
        "the postcode's are provided in the same format" in {
          mockRetrieveSautr(testJourneyId)(Future.successful(Some(testSautr)))
          mockRetrieveSaPostcode(testJourneyId)(Future.successful(Some(testSaPostcode)))
          mockRetrieveKnownFacts(testSautr)(Future.successful(KnownFacts(Some(testSaPostcode), None, Some(testNino))))
          mockStoreIdentifiersMatch(testJourneyId, identifiersMatch = true)(Future.successful(SuccessfullyStored))

          val result = await(TestService.matchKnownFacts(testJourneyId))

          result mustBe true

          verifyRetrieveKnownFacts(testSautr)
          verifyStoreIdentifiersMatch(testJourneyId, identifiersMatch = true)
        }
        "the postcode's are provided in different formats" in {
          val testPostcode: String = "aa1 1aa"
          mockRetrieveSautr(testJourneyId)(Future.successful(Some(testSautr)))
          mockRetrieveSaPostcode(testJourneyId)(Future.successful(Some(testPostcode)))
          mockRetrieveKnownFacts(testSautr)(Future.successful(KnownFacts(Some(testSaPostcode), None, Some(testNino))))
          mockStoreIdentifiersMatch(testJourneyId, identifiersMatch = true)(Future.successful(SuccessfullyStored))

          val result = await(TestService.matchKnownFacts(testJourneyId))

          result mustBe true

          verifyRetrieveKnownFacts(testSautr)
          verifyStoreIdentifiersMatch(testJourneyId, identifiersMatch = true)
        }
      }
      "the user does not provide a postcode and isAbroad is true" in {
        mockRetrieveSautr(testJourneyId)(Future.successful(Some(testSautr)))
        mockRetrieveSaPostcode(testJourneyId)(Future.successful(None))
        mockRetrieveKnownFacts(testSautr)(Future.successful(KnownFacts(Some(testSaPostcode), Some(true), Some(testNino))))
        mockStoreIdentifiersMatch(testJourneyId, identifiersMatch = true)(Future.successful(SuccessfullyStored))

        val result = await(TestService.matchKnownFacts(testJourneyId))

        result mustBe true

        verifyRetrieveKnownFacts(testSautr)
        verifyStoreIdentifiersMatch(testJourneyId, identifiersMatch = true)
      }
    }
    "return false" when {
      "the user's postcode does not match the postcode returned from ES20" in {
        mockRetrieveSautr(testJourneyId)(Future.successful(Some(testSautr)))
        mockRetrieveSaPostcode(testJourneyId)(Future.successful(Some(testSaPostcode)))
        mockRetrieveKnownFacts(testSautr)(Future.successful(KnownFacts(Some("TF4 3ER"), None, Some(testNino))))
        mockStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)(Future.successful(SuccessfullyStored))

        val result = await(TestService.matchKnownFacts(testJourneyId))

        result mustBe false

        verifyRetrieveKnownFacts(testSautr)
        verifyStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)
      }
      "the user does not provide a postcode but isAbroad is false" in {
        mockRetrieveSautr(testJourneyId)(Future.successful(Some(testSautr)))
        mockRetrieveSaPostcode(testJourneyId)(Future.successful(None))
        mockRetrieveKnownFacts(testSautr)(Future.successful(KnownFacts(Some(testSaPostcode), Some(false), Some(testNino))))
        mockStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)(Future.successful(SuccessfullyStored))

        val result = await(TestService.matchKnownFacts(testJourneyId))

        result mustBe false

        verifyRetrieveKnownFacts(testSautr)
        verifyStoreIdentifiersMatch(testJourneyId, identifiersMatch = false)
      }
    }
    "throw an exception" when {
      "the user does not have an sautr" in {
        mockRetrieveSautr(testJourneyId)(Future.successful(None))
        mockRetrieveSaPostcode(testJourneyId)(Future.successful(Some(testSaPostcode)))

        intercept[InternalServerException](
          await(TestService.matchKnownFacts(testJourneyId))
        )
      }
    }
  }

}
