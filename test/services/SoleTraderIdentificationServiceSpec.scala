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

import connectors.mocks.MockSoleTraderIdentificationConnector
import helpers.TestConstants.{testJourneyId, testSautr}
import play.api.test.Helpers._
import uk.gov.hmrc.http.{GatewayTimeoutException, HeaderCarrier}
import uk.gov.hmrc.soletraderidentificationfrontend.services.SoleTraderIdentificationService
import utils.UnitSpec

import scala.concurrent.Future

class SoleTraderIdentificationServiceSpec extends UnitSpec with MockSoleTraderIdentificationConnector {

  object TestService extends SoleTraderIdentificationService(mockSoleTraderIdentificationConnector)

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val dataKey = "sautr"

  "retrieveSautr" should {
    "return Some(sautr)" when {
      "the sautr exists in the database for a given journey id" in {
        mockRetrieveSoleTraderInformation[String](
          testJourneyId,
          dataKey
        )(Future.successful(Some(testSautr)))

        val result = await(TestService.retrieveSautr(testJourneyId))

        result mustBe Some(testSautr)
        verifyRetrieveSoleTraderInformation[String](testJourneyId, dataKey)
      }
    }

    "return None" when {
      "the sautr does not exist in the database for a given journey id" in {
        mockRetrieveSoleTraderInformation[String](
          testJourneyId,
          dataKey
        )(Future.successful(None))

        val result = await(TestService.retrieveSautr(testJourneyId))

        result mustBe None
        verifyRetrieveSoleTraderInformation[String](testJourneyId, dataKey)
      }
    }

    "surface an exception" when {
      "the call to the database times out" in {
        mockRetrieveSoleTraderInformation[String](
          journeyId = testJourneyId,
          dataKey = "sautr"
        )(Future.failed(new GatewayTimeoutException("GET of '/testUrl' timed out with message 'testError'")))

        intercept[GatewayTimeoutException](
          await(TestService.retrieveSautr(testJourneyId))
        )
        verifyRetrieveSoleTraderInformation[String](testJourneyId, dataKey)
      }
    }
  }

}
