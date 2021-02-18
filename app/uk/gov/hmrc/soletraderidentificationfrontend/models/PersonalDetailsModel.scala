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

import java.time.LocalDate

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, OFormat, OWrites, Reads}

case class PersonalDetailsModel(firstName: String, lastName: String, dateOfBirth: LocalDate)

object PersonalDetailsModel {

  val writes: OWrites[PersonalDetailsModel] = (
    (JsPath \ firstNameKey).write[String] and
      (JsPath \ lastNameKey).write[String] and
      (JsPath \ dateOfBirthKey).write[LocalDate]
    )(unlift(PersonalDetailsModel.unapply))

  val reads: Reads[PersonalDetailsModel] = (
    (JsPath \ firstNameKey).read[String] and
      (JsPath \ lastNameKey).read[String] and
      (JsPath \ dateOfBirthKey).read[LocalDate]
    )(PersonalDetailsModel.apply _)

  implicit val format: OFormat[PersonalDetailsModel] = OFormat(reads, writes)

}