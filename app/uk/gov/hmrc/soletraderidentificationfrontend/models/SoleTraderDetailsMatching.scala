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

object SoleTraderDetailsMatching {
  type AuthenticatorResponse = Either[SoleTraderDetailsMatchFailure, AuthenticatorDetails]

  type SoleTraderVerificationResult = Either[SoleTraderDetailsMatchFailure, Matched.type]

  case object Matched

  sealed trait SoleTraderDetailsMatchFailure

  case object Mismatch extends SoleTraderDetailsMatchFailure

  case object NotFound extends SoleTraderDetailsMatchFailure

  case object Deceased extends SoleTraderDetailsMatchFailure

}
