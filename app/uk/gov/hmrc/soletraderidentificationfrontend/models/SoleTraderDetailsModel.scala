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
import play.api.libs.json.{JsPath, OFormat, OWrites, Reads}

import java.time.LocalDate

case class SoleTraderDetailsModel(firstName: String,
                                  lastName: String,
                                  dateOfBirth: LocalDate,
                                  nino: String,
                                  optSautr: Option[String])

object SoleTraderDetailsModel {

  private val FullNameKey = "fullName"
  private val FirstNameKey = "firstName"
  private val LastNameKey = "lastName"
  private val NinoKey = "nino"
  private val SautrKey = "sautr"
  private val DateOfBirthKey = "dateOfBirth"

  implicit val reads: Reads[SoleTraderDetailsModel] = (
    (JsPath \ FullNameKey \ FirstNameKey).read[String] and
      (JsPath \ FullNameKey \ LastNameKey).read[String] and
      (JsPath \ DateOfBirthKey).read[LocalDate] and
      (JsPath \ NinoKey).read[String] and
      (JsPath \ SautrKey).readNullable[String]
    ) (SoleTraderDetailsModel.apply _)

  implicit val writes: OWrites[SoleTraderDetailsModel] = (
    (JsPath \ FullNameKey \ FirstNameKey).write[String] and
      (JsPath \ FullNameKey \ LastNameKey).write[String] and
      (JsPath \ DateOfBirthKey).write[LocalDate] and
      (JsPath \ NinoKey).write[String] and
      (JsPath \ SautrKey).writeNullable[String]
    ) (unlift(SoleTraderDetailsModel.unapply))


  val format: OFormat[SoleTraderDetailsModel] = OFormat(reads, writes)

}
