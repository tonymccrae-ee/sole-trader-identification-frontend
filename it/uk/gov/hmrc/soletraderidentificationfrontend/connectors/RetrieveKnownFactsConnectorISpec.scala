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

package uk.gov.hmrc.soletraderidentificationfrontend.connectors

import play.api.test.Helpers.{NO_CONTENT, OK, await, defaultAwaitTimeout}
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.soletraderidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.soletraderidentificationfrontend.featureswitch.core.config.KnownFactsStub
import uk.gov.hmrc.soletraderidentificationfrontend.models.KnownFactsResponse
import uk.gov.hmrc.soletraderidentificationfrontend.stubs.KnownFactsStub
import uk.gov.hmrc.soletraderidentificationfrontend.utils.ComponentSpecHelper

class RetrieveKnownFactsConnectorISpec extends ComponentSpecHelper with KnownFactsStub {

  private val retrieveKnownFactsConnector: RetrieveKnownFactsConnector = app.injector.instanceOf[RetrieveKnownFactsConnector]

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "retrieveKnownFacts" when {
    "the StubKnownFacts is enabled" should {
      "return KnownFacts" when {
        "a nino is not there" in {
          enable(KnownFactsStub)
          stubRetrieveKnownFactsFromStub(testSautr)(OK, testKnownFactsResponse)

          val result = await(retrieveKnownFactsConnector.retrieveKnownFacts(testSautr))

          result mustBe KnownFactsResponse(Some(testSaPostcode), None, None)

          verifyRetrieveKnownFactsFromStub(testSautr)
        }
        "the isAbroad flag is not there" in {
          enable(KnownFactsStub)
          stubRetrieveKnownFactsFromStub(testSautr)(OK, testKnownFactsResponseNino)

          val result = await(retrieveKnownFactsConnector.retrieveKnownFacts(testSautr))

          result mustBe KnownFactsResponse(Some(testSaPostcode), None, Some(testNino))

          verifyRetrieveKnownFactsFromStub(testSautr)
        }
        "the isAbroad flag is there" in {
          enable(KnownFactsStub)
          stubRetrieveKnownFactsFromStub(testSautr)(OK, testKnownFactsResponseIsAbroad())

          val result = await(retrieveKnownFactsConnector.retrieveKnownFacts(testSautr))

          result mustBe KnownFactsResponse(None, Some(true), None)

          verifyRetrieveKnownFactsFromStub(testSautr)
        }
      }
      "throw an exception" in {
        enable(KnownFactsStub)
        stubRetrieveKnownFactsFromStub(testSautr)(NO_CONTENT)

        intercept[InternalServerException](await(retrieveKnownFactsConnector.retrieveKnownFacts(testSautr)))
      }
    }
    "the StubKnownFacts is disabled" should {
      "return KnownFacts" when {
        "a nino is not there" in {
          disable(KnownFactsStub)
          stubRetrieveKnownFacts(testSautr)(OK, testKnownFactsResponse)

          val result = await(retrieveKnownFactsConnector.retrieveKnownFacts(testSautr))

          result mustBe KnownFactsResponse(Some(testSaPostcode), None, None)

          verifyRetrieveKnownFacts(testSautr)
        }
        "the isAbroad flag is not there" in {
          disable(KnownFactsStub)
          stubRetrieveKnownFacts(testSautr)(OK, testKnownFactsResponseNino)

          val result = await(retrieveKnownFactsConnector.retrieveKnownFacts(testSautr))

          result mustBe KnownFactsResponse(Some(testSaPostcode), None, Some(testNino))

          verifyRetrieveKnownFacts(testSautr)
        }
        "the isAbroad flag is there" in {
          disable(KnownFactsStub)
          stubRetrieveKnownFacts(testSautr)(OK, testKnownFactsResponseIsAbroad())

          val result = await(retrieveKnownFactsConnector.retrieveKnownFacts(testSautr))

          result mustBe KnownFactsResponse(None, Some(true), None)

          verifyRetrieveKnownFacts(testSautr)
        }
      }
      "throw an exception" in {
        disable(KnownFactsStub)
        stubRetrieveKnownFacts(testSautr)(NO_CONTENT)

        intercept[InternalServerException](await(retrieveKnownFactsConnector.retrieveKnownFacts(testSautr)))
      }
    }
  }

}
