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

import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.soletraderidentificationfrontend.connectors.CreateJourneyConnector
import uk.gov.hmrc.soletraderidentificationfrontend.models.JourneyConfig
import uk.gov.hmrc.soletraderidentificationfrontend.repositories.JourneyConfigRepository

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class JourneyService @Inject()(createJourneyConnector: CreateJourneyConnector,
                               journeyConfigRepository: JourneyConfigRepository
                              )(implicit ec: ExecutionContext) {

  def createJourney(journeyConfig: JourneyConfig, authInternalId: String)
                   (implicit headerCarrier: HeaderCarrier): Future[String] =
    for {
      journeyId <- createJourneyConnector.createJourney()
      _ <- journeyConfigRepository.insertJourneyConfig(journeyId, authInternalId, journeyConfig)
    } yield journeyId

  def getJourneyConfig(journeyId: String): Future[JourneyConfig] =
    journeyConfigRepository.findById(journeyId).map {
      case Some(journeyConfig) =>
        journeyConfig
      case None =>
        throw new InternalServerException(s"Journey config was not found for journey ID $journeyId")
    }

}
