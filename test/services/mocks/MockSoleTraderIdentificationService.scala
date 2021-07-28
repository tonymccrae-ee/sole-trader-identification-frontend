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

package services.mocks

import org.mockito.ArgumentMatchers
import org.mockito.Mockito.{reset, _}
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.soletraderidentificationfrontend.httpParsers.SoleTraderIdentificationStorageHttpParser.SuccessfullyStored
import uk.gov.hmrc.soletraderidentificationfrontend.models.{BusinessVerificationStatus, RegistrationStatus}
import uk.gov.hmrc.soletraderidentificationfrontend.services.SoleTraderIdentificationService

import scala.concurrent.Future

trait MockSoleTraderIdentificationService extends MockitoSugar with BeforeAndAfterEach {
  self: Suite =>

  val mockSoleTraderIdentificationService: SoleTraderIdentificationService = mock[SoleTraderIdentificationService]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSoleTraderIdentificationService)
  }

  def mockRetrieveSautr(journeyId: String)
                       (response: Future[Option[String]]): OngoingStubbing[_] =
    when(mockSoleTraderIdentificationService.retrieveSautr(
      ArgumentMatchers.eq(journeyId)
    )(ArgumentMatchers.any[HeaderCarrier])
    ).thenReturn(response)

  def mockRetrieveNino(journeyId: String)
                      (response: Future[Option[String]]): OngoingStubbing[_] =
    when(mockSoleTraderIdentificationService.retrieveNino(
      ArgumentMatchers.eq(journeyId)
    )(ArgumentMatchers.any[HeaderCarrier])
    ).thenReturn(response)

  def mockRetrieveBusinessVerificationResponse(journeyId: String)
                                              (response: Future[Option[BusinessVerificationStatus]]): OngoingStubbing[_] =
    when(mockSoleTraderIdentificationService.retrieveBusinessVerificationStatus(
      ArgumentMatchers.eq(journeyId)
    )(ArgumentMatchers.any[HeaderCarrier])
    ).thenReturn(response)

  def mockStoreRegistrationResponse(journeyId: String, registrationStatus: RegistrationStatus)
                                   (response: Future[SuccessfullyStored.type]): OngoingStubbing[_] =
    when(mockSoleTraderIdentificationService.storeRegistrationStatus(
      ArgumentMatchers.eq(journeyId),
      ArgumentMatchers.eq(registrationStatus)
    )(ArgumentMatchers.any[HeaderCarrier])
    ).thenReturn(response)

  def mockStoreBusinessVerificationStatus(journeyId: String, businessVerificationStatus: BusinessVerificationStatus)
                                         (response: Future[SuccessfullyStored.type]): OngoingStubbing[_] =
    when(mockSoleTraderIdentificationService.storeBusinessVerificationStatus(
      ArgumentMatchers.eq(journeyId),
      ArgumentMatchers.eq(businessVerificationStatus)
    )(ArgumentMatchers.any[HeaderCarrier])
    ).thenReturn(response)

  def mockStoreIdentifiersMatch(journeyId: String, identifiersMatch: Boolean)
                               (response: Future[SuccessfullyStored.type]): OngoingStubbing[_] =
    when(mockSoleTraderIdentificationService.storeIdentifiersMatch(
      ArgumentMatchers.eq(journeyId),
      ArgumentMatchers.eq(identifiersMatch)
    )(ArgumentMatchers.any[HeaderCarrier])
    ).thenReturn(response)

  def verifyStoreRegistrationResponse(journeyId: String, registrationStatus: RegistrationStatus): Unit =
    verify(mockSoleTraderIdentificationService).storeRegistrationStatus(
      ArgumentMatchers.eq(journeyId),
      ArgumentMatchers.eq(registrationStatus)
    )(ArgumentMatchers.any[HeaderCarrier])

  def verifyStoreBusinessVerificationStatus(journeyId: String, businessVerificationStatus: BusinessVerificationStatus): Unit =
    verify(mockSoleTraderIdentificationService).storeBusinessVerificationStatus(
      ArgumentMatchers.eq(journeyId),
      ArgumentMatchers.eq(businessVerificationStatus)
    )(ArgumentMatchers.any[HeaderCarrier])

  def verifyStoreIdentifiersMatch(journeyId: String, identifiersMatch: Boolean): Unit =
    verify(mockSoleTraderIdentificationService).storeIdentifiersMatch(
      ArgumentMatchers.eq(journeyId),
      ArgumentMatchers.eq(identifiersMatch)
    )(ArgumentMatchers.any[HeaderCarrier])

}
