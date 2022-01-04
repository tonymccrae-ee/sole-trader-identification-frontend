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
import uk.gov.hmrc.soletraderidentificationfrontend.connectors.RegistrationConnector
import uk.gov.hmrc.soletraderidentificationfrontend.models.{BusinessVerificationPass, RegistrationNotCalled, RegistrationStatus, SaEnrolled}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RegistrationOrchestrationService @Inject()(soleTraderIdentificationService: SoleTraderIdentificationService,
                                                 registrationConnector: RegistrationConnector,
                                                 trnService: CreateTrnService,
                                                 auditService: AuditService
                                                )(implicit ec: ExecutionContext) {

  def register(journeyId: String)(implicit hc: HeaderCarrier): Future[RegistrationStatus] = for {
    registrationStatus <- soleTraderIdentificationService.retrieveBusinessVerificationStatus(journeyId).flatMap {
      case Some(BusinessVerificationPass) | Some(SaEnrolled) => for {
        optNino <- soleTraderIdentificationService.retrieveNino(journeyId)
        sautr <- soleTraderIdentificationService.retrieveSautr(journeyId).map(_.getOrElse(noDataServerException(journeyId)))
        registrationStatus <- register(journeyId, optNino, sautr)
      } yield registrationStatus
      case Some(_) =>
        Future.successful(RegistrationNotCalled)
      case None =>
        throw new InternalServerException(s"Missing business verification state in database for $journeyId")
    }
    _ <- soleTraderIdentificationService.storeRegistrationStatus(journeyId, registrationStatus)
  } yield {
    auditService.auditSoleTraderJourney(journeyId)
    registrationStatus
  }

  def registerWithoutBusinessVerification(journeyId: String, optNino: Option[String], saUtr: String)
                                         (implicit hc: HeaderCarrier): Future[RegistrationStatus] = for {
    registrationStatus <- register(journeyId, optNino, saUtr)
    _ <- soleTraderIdentificationService.storeRegistrationStatus(journeyId, registrationStatus)
  } yield {
    auditService.auditSoleTraderJourney(journeyId)
    registrationStatus
  }

  private def register(journeyId: String, optNino: Option[String], saUtr: String)(implicit hc: HeaderCarrier): Future[RegistrationStatus] =
    (optNino, saUtr) match {
      case (Some(nino), sautr) =>
        registrationConnector.registerWithNino(nino, sautr)
      case (None, sautr) =>
        trnService.createTrn(journeyId) flatMap {
          trn => registrationConnector.registerWithTrn(trn, sautr)
        }
    }

  private def noDataServerException(journeyId: String): Nothing =
    throw new InternalServerException(s"Missing required data for registration in database for $journeyId")
}
