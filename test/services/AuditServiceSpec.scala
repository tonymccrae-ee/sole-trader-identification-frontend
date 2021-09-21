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

import connectors.mocks.MockAuditConnector
import helpers.TestConstants._
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.test.Helpers._
import services.mocks.MockSoleTraderIdentificationService
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.soletraderidentificationfrontend.models.SoleTraderDetailsMatching.DetailsMismatch
import uk.gov.hmrc.soletraderidentificationfrontend.services.AuditService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AuditServiceSpec extends AnyWordSpec with Matchers with MockAuditConnector with MockSoleTraderIdentificationService {

  object TestService extends AuditService(mockAuditConnector, mockSoleTraderIdentificationService)

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "auditIndividualJourney" should {
    "send an event" when {
      "the entity is an individual and identifiers match" in {
        mockRetrieveFullName(testJourneyId)(Future.successful(Some(testFullName)))
        mockRetrieveDateOfBirth(testJourneyId)(Future.successful(Some(testDateOfBirth)))
        mockRetrieveNino(testJourneyId)(Future.successful(Some(testNino)))
        mockRetrieveIdentifiersMatch(testJourneyId)(Future.successful(Some(true)))
        mockRetrieveAuthenticatorDetails(testJourneyId)(Future.successful(Some(testIndividualDetailsNoSautr)))

        val result: Unit = await(TestService.auditIndividualJourney(testJourneyId))

        result mustBe()

        verifySendExplicitAuditIndividuals()
        auditEventCaptor.getValue mustBe testIndividualSuccessfulAuditEventJson
      }

      "the entity is an individual and identifiers do not match" in {
        mockRetrieveFullName(testJourneyId)(Future.successful(Some(testFullName)))
        mockRetrieveDateOfBirth(testJourneyId)(Future.successful(Some(testDateOfBirth)))
        mockRetrieveNino(testJourneyId)(Future.successful(Some(testNino)))
        mockRetrieveIdentifiersMatch(testJourneyId)(Future.successful(Some(false)))
        mockRetrieveAuthenticatorFailureResponse(testJourneyId)(Future.successful(Some(DetailsMismatch.toString)))

        val result: Unit = await(TestService.auditIndividualJourney(testJourneyId))

        result mustBe()

        verifySendExplicitAuditIndividuals()
        auditEventCaptor.getValue mustBe testIndividualFailureAuditEventJson
      }
    }

    "throw an exception" when {
      "there is missing data for the audit" in {
        mockRetrieveFullName(testJourneyId)(Future.failed(new InternalServerException("failed")))

        intercept[InternalServerException](
          await(TestService.auditIndividualJourney(testJourneyId))
        )
      }
    }
  }

  "auditSoleTraderJourney" should {
    "send an event" when {
      "the entity is a Sole Trader and identifiers match" when {
        "there is an sautr" in {
          mockRetrieveSoleTraderDetails(testJourneyId)(Future.successful(Some(testSoleTraderDetailsNinoAndUtr)))
          mockRetrieveIdentifiersMatch(testJourneyId)(Future.successful(Some(true)))
          mockRetrieveAuthenticatorDetails(testJourneyId)(Future.successful(Some(testIndividualDetails)))
          mockRetrieveES20Response(testJourneyId)(Future.successful(None))


          val result: Unit = await(TestService.auditSoleTraderJourney(testJourneyId))

          result mustBe()

          verifySendExplicitAuditSoleTraders()
          auditEventCaptor.getValue mustBe testSoleTraderAuditEventJson(identifiersMatch = true)
        }
        "there is not an sautr" in {
          mockRetrieveSoleTraderDetails(testJourneyId)(Future.successful(Some(testSoleTraderDetailsNoSautr)))
          mockRetrieveIdentifiersMatch(testJourneyId)(Future.successful(Some(true)))
          mockRetrieveAuthenticatorDetails(testJourneyId)(Future.successful(Some(testIndividualDetailsNoSautr)))
          mockRetrieveES20Response(testJourneyId)(Future.successful(None))

          val result: Unit = await(TestService.auditSoleTraderJourney(testJourneyId))

          result mustBe()

          verifySendExplicitAuditSoleTraders()
          auditEventCaptor.getValue mustBe testSoleTraderAuditEventJsonNoSautr(true)
        }
        "there is not a nino" in {
          mockRetrieveSoleTraderDetails(testJourneyId)(Future.successful(Some(testSoleTraderDetailsNoNinoButUtr)))
          mockRetrieveIdentifiersMatch(testJourneyId)(Future.successful(Some(true)))
          mockRetrieveAuthenticatorDetails(testJourneyId)(Future.successful(Some(testIndividualDetailsNoNino)))
          mockRetrieveES20Response(testJourneyId)(Future.successful(Some(testKnownFactsResponseUK)))

          val result: Unit = await(TestService.auditSoleTraderJourney(testJourneyId))

          result mustBe()

          verifySendExplicitAuditSoleTraders()
          auditEventCaptor.getValue mustBe testSoleTraderAuditEventJsonNoNino(identifiersMatch = true)
        }
      }
      "the entity is a Sole Trader and identifiers do not match" in {
        mockRetrieveSoleTraderDetails(testJourneyId)(Future.successful(Some(testSoleTraderDetailsNoMatch)))
        mockRetrieveIdentifiersMatch(testJourneyId)(Future.successful(Some(false)))
        mockRetrieveAuthenticatorFailureResponse(testJourneyId)(Future.successful(Some(DetailsMismatch.toString)))
        mockRetrieveES20Response(testJourneyId)(Future.successful(None))

        val result: Unit = await(TestService.auditSoleTraderJourney(testJourneyId))

        result mustBe()

        verifySendExplicitAuditSoleTraders()
        auditEventCaptor.getValue mustBe testSoleTraderFailureAuditEventJson()
      }
      "the entity is a Sole Trader and the user is overseas" in {
        mockRetrieveSoleTraderDetails(testJourneyId)(Future.successful(Some(testSoleTraderDetailsNoNinoAndOverseas)))
        mockRetrieveIdentifiersMatch(testJourneyId)(Future.successful(Some(true)))
        mockRetrieveAuthenticatorDetails(testJourneyId)(Future.successful(Some(testIndividualDetailsNoNino)))
        mockRetrieveES20Response(testJourneyId)(Future.successful(Some(testKnownFactsResponseOverseas)))


        val result: Unit = await(TestService.auditSoleTraderJourney(testJourneyId))

        result mustBe()

        verifySendExplicitAuditSoleTraders()
        auditEventCaptor.getValue mustBe testSoleTraderAuditEventJsonNoNinoOverseas(identifiersMatch = true)
      }
    }

    "throw an exception" when {
      "there is missing data for the audit" in {
        mockRetrieveSoleTraderDetails(testJourneyId)(Future.failed(new InternalServerException("failed")))

        intercept[InternalServerException](
          await(TestService.auditSoleTraderJourney(testJourneyId))
        )
      }
    }
  }

}
