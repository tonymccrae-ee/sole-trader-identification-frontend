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

package uk.gov.hmrc.soletraderidentificationfrontend.services

import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.soletraderidentificationfrontend.connectors.AuthenticatorConnector
import uk.gov.hmrc.soletraderidentificationfrontend.models.SoleTraderDetailsMatching.{Matched, Mismatch, SoleTraderVerificationResult}
import uk.gov.hmrc.soletraderidentificationfrontend.models.{IndividualDetails, JourneyConfig}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuthenticatorService @Inject()(authenticatorConnector: AuthenticatorConnector,
                                     soleTraderIdentificationService: SoleTraderIdentificationService)(implicit ec: ExecutionContext) {

  def matchSoleTraderDetails(journeyId: String,
                             userDetails: IndividualDetails,
                             journeyConfig: JourneyConfig
                            )(implicit hc: HeaderCarrier): Future[SoleTraderVerificationResult] =
    authenticatorConnector.matchSoleTraderDetails(userDetails).flatMap {
      case Right(authenticatorDetails) if journeyConfig.pageConfig.enableSautrCheck =>
        if (authenticatorDetails.optSautr == userDetails.optSautr) {
          soleTraderIdentificationService.storeAuthenticatorDetails(journeyId, authenticatorDetails).map {
            _ => Right(Matched)
          }
        } else
          Future.successful(Left(Mismatch))
      case Right(authenticatorDetails) =>
        soleTraderIdentificationService.storeAuthenticatorDetails(journeyId, authenticatorDetails).map {
          _ => Right(Matched)
        }
      case Left(failureReason) =>
        Future.successful(Left(failureReason))
    }

}
