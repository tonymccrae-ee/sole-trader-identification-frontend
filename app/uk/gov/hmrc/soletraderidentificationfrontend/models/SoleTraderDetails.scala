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

case class SoleTraderDetails(firstName: String,
                             lastName: String,
                             dateOfBirth: LocalDate,
                             nino: String,
                             optSautr: Option[String],
                             businessVerification: BusinessVerificationStatus)

object SoleTraderDetails {

  private val FirstNameKey = "firstName"
  private val LastNameKey = "lastName"
  private val NinoKey = "nino"
  private val SautrKey = "sautr"
  private val DateOfBirthKey = "dateOfBirth"
  private val BusinessVerificationKey = "businessVerification"

  implicit val reads: Reads[SoleTraderDetails] = (
    (JsPath \ FirstNameKey).read[String] and
      (JsPath \ LastNameKey).read[String] and
      (JsPath \ DateOfBirthKey).read[LocalDate] and
      (JsPath \ NinoKey).read[String] and
      (JsPath \ SautrKey).readNullable[String] and
      (JsPath \ BusinessVerificationKey).read[BusinessVerificationStatus]
    ) (SoleTraderDetails.apply _)

  implicit val writes: OWrites[SoleTraderDetails] = (
    (JsPath \ FirstNameKey).write[String] and
      (JsPath \ LastNameKey).write[String] and
      (JsPath \ DateOfBirthKey).write[LocalDate] and
      (JsPath \ NinoKey).write[String] and
      (JsPath \ SautrKey).writeNullable[String] and
      (JsPath \ BusinessVerificationKey).write[BusinessVerificationStatus]
    ) (unlift(SoleTraderDetails.unapply))

  val format: OFormat[SoleTraderDetails] = OFormat(reads, writes)

}
