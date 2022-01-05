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

import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.soletraderidentificationfrontend.models.IndividualDetails

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuditService @Inject()(auditConnector: AuditConnector, soleTraderIdentificationService: SoleTraderIdentificationService) {

  def auditIndividualJourney(journeyId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] = {
    for {
      optFullName <- soleTraderIdentificationService.retrieveFullName(journeyId)
      optDateOfBirth <- soleTraderIdentificationService.retrieveDateOfBirth(journeyId)
      optNino <- soleTraderIdentificationService.retrieveNino(journeyId)
      optIdentifiersMatch <- soleTraderIdentificationService.retrieveIdentifiersMatch(journeyId)
      optAuthenticatorResponse <-
        optIdentifiersMatch match {
          case Some(true) =>
            soleTraderIdentificationService.retrieveAuthenticatorDetails(journeyId)
          case Some(_) if optNino.isEmpty => Future.successful(None)
          case _ =>
            soleTraderIdentificationService.retrieveAuthenticatorFailureResponse(journeyId)
        }
    } yield {
      (optFullName, optDateOfBirth, optNino, optIdentifiersMatch, optAuthenticatorResponse) match {
        case (Some(fullName), Some(dateOfBirth), Some(nino), Some(identifiersMatch), Some(authenticatorDetails: IndividualDetails)) =>
          Json.obj(
            "firstName" -> fullName.firstName,
            "lastName" -> fullName.lastName,
            "nino" -> nino,
            "dateOfBirth" -> dateOfBirth,
            "identifiersMatch" -> identifiersMatch,
            "authenticatorResponse" -> Json.toJson(authenticatorDetails)
          )
        case (Some(fullName), Some(dateOfBirth), Some(nino), Some(identifiersMatch), Some(authenticatorFailureResponse: String)) =>
          Json.obj(
            "firstName" -> fullName.firstName,
            "lastName" -> fullName.lastName,
            "nino" -> nino,
            "dateOfBirth" -> dateOfBirth,
            "identifiersMatch" -> identifiersMatch,
            "authenticatorResponse" -> authenticatorFailureResponse
          )
        case (Some(fullName), Some(dateOfBirth), Some(nino), Some(identifiersMatch), None) =>
          Json.obj(
            "firstName" -> fullName.firstName,
            "lastName" -> fullName.lastName,
            "nino" -> nino,
            "dateOfBirth" -> dateOfBirth,
            "identifiersMatch" -> identifiersMatch
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

  def auditSoleTraderJourney(journeyId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] = {
    for {
      optSoleTraderRecord <- soleTraderIdentificationService.retrieveSoleTraderDetails(journeyId)
      optES20Response <- soleTraderIdentificationService.retrieveES20Details(journeyId)
      optIdentifiersMatch <- soleTraderIdentificationService.retrieveIdentifiersMatch(journeyId)
      optAuthenticatorResponse <-
        optIdentifiersMatch match {
          case Some(_) if optSoleTraderRecord.exists(details => details.optNino.isEmpty) => Future.successful(None)
          case Some(identifiersMatch) if identifiersMatch =>
            soleTraderIdentificationService.retrieveAuthenticatorDetails(journeyId)
          case _ =>
            soleTraderIdentificationService.retrieveAuthenticatorFailureResponse(journeyId)
        }
    } yield {
      (optSoleTraderRecord, optES20Response, optIdentifiersMatch, optAuthenticatorResponse) match {
        case (Some(optSoleTraderRecord), optES20Response, Some(identifiersMatch), optAuthenticatorResponse) =>
          val sautrBlock =
            optSoleTraderRecord.optSautr match {
              case Some(sautr) => Json.obj("userSAUTR" -> sautr)
              case _ => Json.obj()
            }

          val ninoBlock =
            optSoleTraderRecord.optNino match {
              case Some(nino) => Json.obj("nino" -> nino)
              case _ => Json.obj()
            }

          val addressBlock =
            optSoleTraderRecord.address match {
              case Some(address) => Json.obj("address" -> Json.toJson(address))
              case _ => Json.obj()
            }

          val trnBlock =
            optSoleTraderRecord.optTrn match {
              case Some(trn) => Json.obj("TempNI" -> trn)
              case _ => Json.obj()
            }

          val saPostCodeBlock =
            optSoleTraderRecord.optSaPostcode match {
              case Some(postcode) => Json.obj("SAPostcode" -> postcode)
              case _ => Json.obj()
            }

          val eS20Block =
            optES20Response match {
              case Some(eSReponse) => Json.obj("ES20Response" -> eSReponse)
              case _ => Json.obj()
            }

          val authenticatorResponseBlock =
            optAuthenticatorResponse match {
              case Some(authenticatorDetails: IndividualDetails) => Json.obj("authenticatorResponse" -> Json.toJson(authenticatorDetails))
              case Some(authenticatorFailureResponse: String) => Json.obj("authenticatorResponse" -> authenticatorFailureResponse)
              case _ => Json.obj()
            }

          val overseasIdentifiersBlock =
            optSoleTraderRecord.optOverseas match {
              case Some(overseas) => Json.obj(
                "overseasTaxIdentifier" -> overseas.taxIdentifier,
                "overseasTaxIdentifierCountry" -> overseas.country)
              case _ => Json.obj()
            }

          Json.obj(
            "businessType" -> "Sole Trader",
            "firstName" -> optSoleTraderRecord.fullName.firstName,
            "lastName" -> optSoleTraderRecord.fullName.lastName,
            "dateOfBirth" -> optSoleTraderRecord.dateOfBirth,
            "sautrMatch" -> identifiersMatch,
            "VerificationStatus" -> optSoleTraderRecord.businessVerification,
            "RegisterApiStatus" -> optSoleTraderRecord.registrationStatus
          ) ++ sautrBlock ++ ninoBlock ++ addressBlock ++ saPostCodeBlock ++ overseasIdentifiersBlock ++ trnBlock ++ eS20Block ++ authenticatorResponseBlock
        case _ =>
          throw new InternalServerException(s"Not enough information to audit sole trader journey for Journey ID $journeyId")
      }
    }
  }.map {
    auditJson =>
      auditConnector.sendExplicitAudit(
        auditType = "SoleTraderRegistration",
        detail = auditJson
      )
  }
}
