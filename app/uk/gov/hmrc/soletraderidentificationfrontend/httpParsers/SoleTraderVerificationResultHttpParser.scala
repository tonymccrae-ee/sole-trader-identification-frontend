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

package uk.gov.hmrc.soletraderidentificationfrontend.httpParsers

import play.api.http.Status.{FAILED_DEPENDENCY, OK, UNAUTHORIZED}
import uk.gov.hmrc.http.{HttpReads, HttpResponse, InternalServerException}
import uk.gov.hmrc.soletraderidentificationfrontend.models.SoleTraderDetailsMatching._

object SoleTraderVerificationResultHttpParser {
  val notFoundError: String = "cid_no_nino"

  implicit object SoleTraderVerificationResultReads extends HttpReads[SoleTraderVerificationResult] {
    override def read(method: String, url: String, response: HttpResponse): SoleTraderVerificationResult =
      response.status match {
        case OK => Right(Matched)
        case FAILED_DEPENDENCY => Left(Deceased)
        case UNAUTHORIZED =>
          val errors = (response.json \ "errors").as[Seq[String]]

          if (errors.contains(notFoundError)) {
            Left(NotFound)
          } else {
            Left(Mismatch)
          }
        case status =>
          throw new InternalServerException(s"Invalid status received from authenticator#match API: $status")
      }
  }
}
