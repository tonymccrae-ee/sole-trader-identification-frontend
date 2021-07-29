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
import uk.gov.hmrc.soletraderidentificationfrontend.models.SoleTraderDetailsMatching.{Matched, NotFound}
import uk.gov.hmrc.soletraderidentificationfrontend.models._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class OrchestrationService @Inject()(journeyService: JourneyService,
                                     authenticatorService: AuthenticatorService,
                                     soleTraderIdentificationService: SoleTraderIdentificationService,
                                     auditService: AuditService) {

  def orchestrate(journeyId: String,
                  individualDetails: IndividualDetails
                 )(implicit hc: HeaderCarrier,
                   ec: ExecutionContext): Future[OrchestrationResponse] =

    journeyService.getJourneyConfig(journeyId).flatMap {
      journeyConfig =>
        authenticatorService.matchSoleTraderDetails(journeyId, individualDetails, journeyConfig).flatMap {
          case Right(Matched) =>
            individualDetails.optSautr match {
              case Some(_) =>
                soleTraderIdentificationService.storeIdentifiersMatch(journeyId, identifiersMatch = true).map {
                  _ => SautrMatched
                }
              case None =>
                for {
                  _ <- soleTraderIdentificationService.storeIdentifiersMatch(journeyId, identifiersMatch = true)
                  _ <- soleTraderIdentificationService.storeBusinessVerificationStatus(journeyId, BusinessVerificationUnchallenged)
                  _ <- soleTraderIdentificationService.storeRegistrationStatus(journeyId, RegistrationNotCalled)

                } yield {
                  auditService.auditIndividualJourney(journeyId)
                  NoSautrProvided
                }
            }
          case Left(SoleTraderDetailsMatching.NotFound) => soleTraderIdentificationService.storeIdentifiersMatch(journeyId, identifiersMatch = false).map {
            _ => DetailsNotFound
          }
          case _ =>
            soleTraderIdentificationService.storeIdentifiersMatch(journeyId, identifiersMatch = false).map {
              _ => DetailsMismatch
            }
        }
    }

}
