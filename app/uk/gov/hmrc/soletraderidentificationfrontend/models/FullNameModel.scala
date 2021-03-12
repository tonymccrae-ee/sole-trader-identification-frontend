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

case class FullNameModel(firstName: String, lastName: String)

object FullNameModel {
  private val FirstNameKey = "firstName"
  private val LastNameKey = "lastName"

  val writes: OWrites[FullNameModel] = (
    (JsPath \ FirstNameKey).write[String] and
      (JsPath \ LastNameKey).write[String]
    ) (unlift(FullNameModel.unapply))

  val reads: Reads[FullNameModel] = (
    (JsPath \ FirstNameKey).read[String] and
      (JsPath \ LastNameKey).read[String]
    ) (FullNameModel.apply _)

  implicit val format: OFormat[FullNameModel] = OFormat(reads, writes)

}
