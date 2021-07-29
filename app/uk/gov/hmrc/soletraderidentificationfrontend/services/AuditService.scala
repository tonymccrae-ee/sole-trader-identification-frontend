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

import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.soletraderidentificationfrontend.models.EntityType.Individual

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuditService @Inject()(auditConnector: AuditConnector,
                             journeyService: JourneyService,
                             soleTraderIdentificationService: SoleTraderIdentificationService) {


  def auditIndividualJourney(journeyId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] = {
    for {
      journeyConfig <- journeyService.getJourneyConfig(journeyId)
      if journeyConfig.entityType == Individual
      optFullName <- soleTraderIdentificationService.retrieveFullName(journeyId)
      optDateOfBirth <- soleTraderIdentificationService.retrieveDateOfBirth(journeyId)
      optNino <- soleTraderIdentificationService.retrieveNino(journeyId)
      optIdentifiersMatch <- soleTraderIdentificationService.retrieveIdentifiersMatch(journeyId)
      optAuthenticatorDetails <- soleTraderIdentificationService.retrieveAuthenticatorDetails(journeyId)
    } yield {
      (optFullName, optDateOfBirth, optNino, optIdentifiersMatch, optAuthenticatorDetails) match {
        case (Some(fullName), Some(dateOfBirth), Some(nino), Some(identifiersMatch), Some(authenticatorDetails)) =>
          Json.obj(
            "firstName" -> fullName.firstName,
            "lastName" -> fullName.lastName,
            "nino" -> nino,
            "dateOfBirth" -> dateOfBirth,
            "identifiersMatch" -> identifiersMatch,
            "authenticatorResponse" -> Json.toJson(authenticatorDetails)
          )
        case _ =>
          throw new InternalServerException(s"Not enough information to audit individual journey for Journey ID $journeyId")
      }
    }
  }.map {
    auditJson =>
      auditConnector.sendExplicitAudit(
        auditType = "IndividualIdentification",
        detail = auditJson
      )
  }

}
