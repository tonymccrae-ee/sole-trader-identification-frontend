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
import services.mocks.{MockJourneyService, MockSoleTraderIdentificationService}
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.soletraderidentificationfrontend.models.EntityType.{Individual, SoleTrader}
import uk.gov.hmrc.soletraderidentificationfrontend.services.AuditService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AuditServiceSpec extends AnyWordSpec with Matchers with MockAuditConnector with MockJourneyService with MockSoleTraderIdentificationService {

  object TestService extends AuditService(mockAuditConnector, mockJourneyService, mockSoleTraderIdentificationService)

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "auditIndividualJourney" should {
    "send an event" when {
      "the entity is an individual and identifiers match" in {
        mockGetJourneyConfig(testJourneyId)(Future.successful(testJourneyConfig(entityType = Individual)))
        mockRetrieveFullName(testJourneyId)(Future.successful(Some(testFullName)))
        mockRetrieveDateOfBirth(testJourneyId)(Future.successful(Some(testDateOfBirth)))
        mockRetrieveNino(testJourneyId)(Future.successful(Some(testNino)))
        mockRetrieveIdentifiersMatch(testJourneyId)(Future.successful(Some(true)))
        mockRetrieveAuthenticatorDetails(testJourneyId)(Future.successful(Some(testIndividualDetailsNoSautr)))

        val result: Unit = await(TestService.auditIndividualJourney(testJourneyId))

        result mustBe()

        verifySendExplicitAuditIndividuals()
        auditEventCaptor.getValue mustBe testIndividualAuditEventJson(identifiersMatch = true)
      }

      "the entity is an individual and identifiers do not match" in {
        mockGetJourneyConfig(testJourneyId)(Future.successful(testJourneyConfig(entityType = Individual)))
        mockRetrieveFullName(testJourneyId)(Future.successful(Some(testFullName)))
        mockRetrieveDateOfBirth(testJourneyId)(Future.successful(Some(testDateOfBirth)))
        mockRetrieveNino(testJourneyId)(Future.successful(Some(testNino)))
        mockRetrieveIdentifiersMatch(testJourneyId)(Future.successful(Some(false)))
        mockRetrieveAuthenticatorDetails(testJourneyId)(Future.successful(Some(testIndividualDetailsNoSautr)))

        val result: Unit = await(TestService.auditIndividualJourney(testJourneyId))

        result mustBe()

        verifySendExplicitAuditIndividuals()
        auditEventCaptor.getValue mustBe testIndividualAuditEventJson()
      }
    }

    "throw an exception" when {
      "there is missing data for the audit" in {
        mockGetJourneyConfig(testJourneyId)(Future.successful(testJourneyConfig(entityType = Individual)))
        mockRetrieveFullName(testJourneyId)(Future.failed(new InternalServerException("failed")))

        intercept[InternalServerException](
          await(TestService.auditIndividualJourney(testJourneyId))
        )
      }

      "a sole trader is trying to audit as an individual" in {
        mockGetJourneyConfig(testJourneyId)(Future.successful(testJourneyConfig(entityType = SoleTrader)))

        intercept[NoSuchElementException](
          await(TestService.auditIndividualJourney(testJourneyId))
        )
      }
    }
  }

}
