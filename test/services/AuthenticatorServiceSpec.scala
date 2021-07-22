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

import connectors.mocks.MockAuthenticatorConnector
import helpers.TestConstants._
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.soletraderidentificationfrontend.models.SoleTraderDetailsMatching.{Deceased, Matched, Mismatch, NotFound}
import uk.gov.hmrc.soletraderidentificationfrontend.services.AuthenticatorService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AuthenticatorServiceSpec extends AnyWordSpec with Matchers with MockAuthenticatorConnector {

  object TestService extends AuthenticatorService(mockAuthenticatorConnector)

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "matchSoleTraderDetails" should {
    "return Right(Matched)" when {
      "the provided details match those from authenticator" when {
        "the enableSautrCheck is true and the sautr matches the returned one" in {
          mockMatchSoleTraderDetails(testAuthenticatorDetails)(Future.successful(Right(testAuthenticatorDetails)))

          val result = await(TestService.matchSoleTraderDetails(testAuthenticatorDetails, testSoleTraderJourneyConfig(enableSautrCheck = true)))

          result mustBe Right(Matched)
        }

        "the enableSautrCheck is false and the sautr is not provided" in {
          mockMatchSoleTraderDetails(testAuthenticatorDetailsNoSautr)(Future.successful(Right(testAuthenticatorDetailsNoSautr)))

          val result = await(TestService.matchSoleTraderDetails(testAuthenticatorDetailsNoSautr, testSoleTraderJourneyConfig()))

          result mustBe Right(Matched)
        }
      }
    }

    "return Left(Mismatch)" when {
      "the provided details do not match those from authenticator" when {
        "the enableSautrCheck is true and the sautr is provided" in {
          mockMatchSoleTraderDetails(testAuthenticatorDetails)(Future.successful(Left(Mismatch)))

          val result = await(TestService.matchSoleTraderDetails(testAuthenticatorDetails, testSoleTraderJourneyConfig(enableSautrCheck = true)))

          result mustBe Left(Mismatch)
        }

        "the enableSautrCheck is false and the sautr is not provided" in {
          mockMatchSoleTraderDetails(testAuthenticatorDetailsNoSautr)(Future.successful(Left(Mismatch)))

          val result = await(TestService.matchSoleTraderDetails(testAuthenticatorDetailsNoSautr, testSoleTraderJourneyConfig()))

          result mustBe Left(Mismatch)
        }

        "the enableSautrCheck is false and the sautr is provided" in {
          mockMatchSoleTraderDetails(testAuthenticatorDetails)(Future.successful(Left(Mismatch)))

          val result = await(TestService.matchSoleTraderDetails(testAuthenticatorDetails, testSoleTraderJourneyConfig()))

          result mustBe Left(Mismatch)
        }
      }

      "the provided sautr does not exist on authenticator" in {
        mockMatchSoleTraderDetails(testAuthenticatorDetails)(Future.successful(Right(testAuthenticatorDetailsNoSautr)))

        val result = await(TestService.matchSoleTraderDetails(testAuthenticatorDetails, testSoleTraderJourneyConfig(enableSautrCheck = true)))

        result mustBe Left(Mismatch)
      }

      "the user has not provided an sautr but one is returned from authenticator" in {
        mockMatchSoleTraderDetails(testAuthenticatorDetailsNoSautr)(Future.successful(Right(testAuthenticatorDetails)))

        val result = await(TestService.matchSoleTraderDetails(testAuthenticatorDetailsNoSautr, testSoleTraderJourneyConfig(enableSautrCheck = true)))

        result mustBe Left(Mismatch)
      }
    }

    "return Left(NotFound)" when {
      "the users details are not found by authenticator" in {
        mockMatchSoleTraderDetails(testAuthenticatorDetails)(Future.successful(Left(NotFound)))

        val result = await(TestService.matchSoleTraderDetails(testAuthenticatorDetails, testSoleTraderJourneyConfig()))

        result mustBe Left(NotFound)
      }
    }

    "return Left(Deceased)" when {
      "the users details are not found by authenticator" in {
        mockMatchSoleTraderDetails(testAuthenticatorDetails)(Future.successful(Left(Deceased)))

        val result = await(TestService.matchSoleTraderDetails(testAuthenticatorDetails, testSoleTraderJourneyConfig()))

        result mustBe Left(Deceased)
      }
    }
  }

}
