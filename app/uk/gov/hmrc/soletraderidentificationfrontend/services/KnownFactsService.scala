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

import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.soletraderidentificationfrontend.connectors.RetrieveKnownFactsConnector
import uk.gov.hmrc.soletraderidentificationfrontend.models.KnownFacts

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class KnownFactsService @Inject()(soleTraderIdentificationService: SoleTraderIdentificationService,
                                  retrieveKnownFactsConnector: RetrieveKnownFactsConnector) {

  def matchKnownFacts(journeyId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean] = {
    for {
      optUserSautr <- soleTraderIdentificationService.retrieveSautr(journeyId)
      optUserPostcode <- soleTraderIdentificationService.retrieveSaPostcode(journeyId)
      matchingStatus <-
        (optUserSautr, optUserPostcode) match {
          case (Some(sautr), _) =>
            retrieveKnownFactsConnector.retrieveKnownFacts(sautr).map {
              case KnownFacts(Some(retrievePostcode), _, _) if optUserPostcode.exists(userPostcode => userPostcode filterNot (_.isWhitespace) equalsIgnoreCase (retrievePostcode filterNot (_.isWhitespace))) =>
                true
              case KnownFacts(_, Some(true), _) if optUserPostcode.isEmpty =>
                true
              case KnownFacts(_, _, _) =>
                false
            }
          case (_, _) =>
            throw new InternalServerException(s"Missing SAUTR data to retrieve known facts for journeyId: $journeyId")
        }
    } yield {
      soleTraderIdentificationService.storeIdentifiersMatch(journeyId, matchingStatus)
      matchingStatus
    }
  }

}
