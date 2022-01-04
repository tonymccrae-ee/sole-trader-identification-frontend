/*
 * Copyright 2022 HM Revenue & Customs
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
import uk.gov.hmrc.soletraderidentificationfrontend.connectors.{AuthenticatorConnector, RetrieveKnownFactsConnector}
import uk.gov.hmrc.soletraderidentificationfrontend.models.SoleTraderDetailsMatching.{DetailsMismatch, SoleTraderDetailsMatchFailure}
import uk.gov.hmrc.soletraderidentificationfrontend.models.{IndividualDetails, JourneyConfig, KnownFactsResponse}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SoleTraderMatchingService @Inject()(authenticatorConnector: AuthenticatorConnector,
                                          retrieveKnownFactsConnector: RetrieveKnownFactsConnector,
                                          soleTraderIdentificationService: SoleTraderIdentificationService) {

  def matchSoleTraderDetails(journeyId: String,
                             individualDetails: IndividualDetails,
                             journeyConfig: JourneyConfig)(implicit hc: HeaderCarrier,
                                                           ec: ExecutionContext): Future[Either[SoleTraderDetailsMatchFailure, Boolean]] =
    for {
      authenticatorResponse <- authenticatorConnector.matchSoleTraderDetails(individualDetails).map {
        case Right(authenticatorDetails) if journeyConfig.pageConfig.enableSautrCheck =>
          if (authenticatorDetails.optSautr == individualDetails.optSautr)
            Right(authenticatorDetails)
          else {
            Left(DetailsMismatch)
          }
        case authenticatorResponse =>
          authenticatorResponse
      }
      matchingResponse <- authenticatorResponse match {
        case Right(details) =>
          soleTraderIdentificationService.storeAuthenticatorDetails(journeyId, details).map {
            _ => Right(true)
          }
        case Left(failureResponse) =>
          soleTraderIdentificationService.storeAuthenticatorFailureResponse(journeyId, failureResponse).map {
            _ => Left(failureResponse)
          }
      }
      identifiersMatch = if (matchingResponse.isRight) true else false
      _ <- soleTraderIdentificationService.storeIdentifiersMatch(journeyId, identifiersMatch)
    } yield
      matchingResponse

  def matchSoleTraderDetailsNoNino(journeyId: String,
                                   individualDetails: IndividualDetails
                                  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[SoleTraderDetailsMatchFailure, Boolean]] = {
    for {
      optUserPostcode <- soleTraderIdentificationService.retrieveSaPostcode(journeyId)
      matchingResponse <-
        (individualDetails.optSautr, optUserPostcode) match {
          case (Some(sautr), _) =>
            retrieveKnownFactsConnector.retrieveKnownFacts(sautr).flatMap {
              case KnownFacts@KnownFactsResponse(Some(retrievePostcode), _, _) if optUserPostcode.exists(userPostcode => userPostcode filterNot (_.isWhitespace) equalsIgnoreCase (retrievePostcode filterNot (_.isWhitespace))) =>
                soleTraderIdentificationService.storeES20Details(journeyId, KnownFacts).map(
                  _ => Right(true)
                )
              case KnownFacts@KnownFactsResponse(_, Some(true), _) if optUserPostcode.isEmpty =>
                soleTraderIdentificationService.storeES20Details(journeyId, KnownFacts).map(
                  _ => Right(true)
                )
              case KnownFacts@KnownFactsResponse(_, _, _) =>
                soleTraderIdentificationService.storeES20Details(journeyId, KnownFacts).map(
                  _ => Left(DetailsMismatch)
                )
            }
          case (_, _) => Future.successful(Right(false))
        }
      identifiersMatch = matchingResponse match {
        case Right(true) => true
        case _ => false
      }
      _ <- soleTraderIdentificationService.storeIdentifiersMatch(journeyId, identifiersMatch)
    } yield {
      matchingResponse
    }
  }

}
