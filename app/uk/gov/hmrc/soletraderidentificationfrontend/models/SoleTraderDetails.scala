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

package uk.gov.hmrc.soletraderidentificationfrontend.models

import play.api.libs.functional.syntax._
import play.api.libs.json._

import java.time.LocalDate

case class SoleTraderDetails(fullName: FullName,
                             dateOfBirth: LocalDate,
                             optNino: Option[String],
                             optSautr: Option[String],
                             identifiersMatch: Boolean,
                             businessVerification: BusinessVerificationStatus,
                             registrationStatus: RegistrationStatus)

object SoleTraderDetails {

  private val FullNameKey = "fullName"
  private val NinoKey = "nino"
  private val SautrKey = "sautr"
  private val DateOfBirthKey = "dateOfBirth"
  private val IdentifiersMatchKey = "identifiersMatch"
  private val BusinessVerificationKey = "businessVerification"
  private val RegistrationKey = "registration"

  val reads: Reads[SoleTraderDetails] = (
    (JsPath \ FullNameKey).read[FullName] and
      (JsPath \ DateOfBirthKey).read[LocalDate] and
      (JsPath \ NinoKey).readNullable[String] and
      (JsPath \ SautrKey).readNullable[String] and
      (JsPath \ IdentifiersMatchKey).read[Boolean] and
      (JsPath \ BusinessVerificationKey).read[BusinessVerificationStatus] and
      (JsPath \ RegistrationKey).read[RegistrationStatus]
    ) (SoleTraderDetails.apply _)

  val writes: OWrites[SoleTraderDetails] = (
    (JsPath \ FullNameKey).write[FullName] and
      (JsPath \ DateOfBirthKey).write[LocalDate] and
      (JsPath \ NinoKey).writeNullable[String] and
      (JsPath \ SautrKey).writeNullable[String] and
      (JsPath \ IdentifiersMatchKey).write[Boolean] and
      (JsPath \ BusinessVerificationKey).write[BusinessVerificationStatus] and
      (JsPath \ RegistrationKey).write[RegistrationStatus]
    ) (unlift(SoleTraderDetails.unapply))

  implicit val format: OFormat[SoleTraderDetails] = OFormat(reads, writes)

}
