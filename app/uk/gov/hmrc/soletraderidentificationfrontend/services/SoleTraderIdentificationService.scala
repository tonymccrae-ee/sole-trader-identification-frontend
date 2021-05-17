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

import play.api.libs.json.OFormat.oFormatFromReadsAndOWrites
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.soletraderidentificationfrontend.connectors.SoleTraderIdentificationConnector
import uk.gov.hmrc.soletraderidentificationfrontend.httpParsers.RemoveSoleTraderDetailsHttpParser.SuccessfullyRemoved
import uk.gov.hmrc.soletraderidentificationfrontend.httpParsers.SoleTraderIdentificationStorageHttpParser.SuccessfullyStored
import uk.gov.hmrc.soletraderidentificationfrontend.models.{AuthenticatorDetails, BusinessVerificationStatus, FullName, SoleTraderDetails}
import uk.gov.hmrc.soletraderidentificationfrontend.services.SoleTraderIdentificationService._

import java.time.LocalDate
import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class SoleTraderIdentificationService @Inject()(connector: SoleTraderIdentificationConnector) {

  def storeFullName(journeyId: String, fullName: FullName)(implicit hc: HeaderCarrier): Future[SuccessfullyStored.type] =
    connector.storeData[FullName](journeyId, FullNameKey, fullName)

  def storeDateOfBirth(journeyId: String, dateOfBirth: LocalDate)(implicit hc: HeaderCarrier): Future[SuccessfullyStored.type] =
    connector.storeData[LocalDate](journeyId, DateOfBirthKey, dateOfBirth)

  def storeNino(journeyId: String, nino: String)(implicit hc: HeaderCarrier): Future[SuccessfullyStored.type] =
    connector.storeData[String](journeyId, NinoKey, nino)

  def storeSautr(journeyId: String, sautr: String)(implicit hc: HeaderCarrier): Future[SuccessfullyStored.type] =
    connector.storeData[String](journeyId, SautrKey, sautr)

  def retrieveFullName(journeyId: String)(implicit hc: HeaderCarrier): Future[Option[FullName]] =
    connector.retrieveSoleTraderIdentification[FullName](journeyId, FullNameKey)

  def retrieveDateOfBirth(journeyId: String)(implicit hc: HeaderCarrier): Future[Option[LocalDate]] =
    connector.retrieveSoleTraderIdentification[LocalDate](journeyId, DateOfBirthKey)

  def retrieveNino(journeyId: String)(implicit hc: HeaderCarrier): Future[Option[String]] =
    connector.retrieveSoleTraderIdentification[String](journeyId, NinoKey)

  def retrieveSautr(journeyId: String)(implicit hc: HeaderCarrier): Future[Option[String]] =
    connector.retrieveSoleTraderIdentification[String](journeyId, SautrKey)

  def retrieveSoleTraderDetails(journeyId: String)(implicit hc: HeaderCarrier): Future[Option[SoleTraderDetails]] =
    connector.retrieveSoleTraderIdentification(journeyId)

  def retrieveAuthenticatorDetails(journeyId: String)(implicit hc: HeaderCarrier): Future[Option[AuthenticatorDetails]] =
    connector.retrieveAuthenticatorDetails(journeyId)

  def removeSautr(journeyId: String)(implicit hc: HeaderCarrier): Future[SuccessfullyRemoved.type] =
    connector.removeSoleTraderIdentification(journeyId, SautrKey)

  def storeBusinessVerificationStatus(journeyId: String,
                                      businessVerification: BusinessVerificationStatus
                                     )(implicit hc: HeaderCarrier): Future[SuccessfullyStored.type] =
    connector.storeData[BusinessVerificationStatus](journeyId, verificationStatusKey, businessVerification)

}

object SoleTraderIdentificationService {
  private val FullNameKey = "fullName"
  private val NinoKey = "nino"
  private val SautrKey = "sautr"
  private val DateOfBirthKey = "dateOfBirth"
  private val verificationStatusKey = "businessVerification"
}