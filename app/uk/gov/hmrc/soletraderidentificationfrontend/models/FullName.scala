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

case class FullName(firstName: String, lastName: String)

object FullName {
  private val FirstNameKey = "firstName"
  private val LastNameKey = "lastName"

  val writes: OWrites[FullName] = (
    (JsPath \ FirstNameKey).write[String] and
      (JsPath \ LastNameKey).write[String]
    ) (unlift(FullName.unapply))

  val reads: Reads[FullName] = (
    (JsPath \ FirstNameKey).read[String] and
      (JsPath \ LastNameKey).read[String]
    ) (FullName.apply _)

  implicit val format: OFormat[FullName] = OFormat(reads, writes)

}
