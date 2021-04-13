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

package uk.gov.hmrc.soletraderidentificationfrontend.connectors

import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.soletraderidentificationfrontend.config.AppConfig
import uk.gov.hmrc.soletraderidentificationfrontend.httpParsers.SoleTraderVerificationResultHttpParser.SoleTraderVerificationResultReads
import uk.gov.hmrc.soletraderidentificationfrontend.models.SoleTraderDetails
import uk.gov.hmrc.soletraderidentificationfrontend.models.SoleTraderDetailsMatching.SoleTraderVerificationResult

import java.time.format.DateTimeFormatter.ofPattern
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuthenticatorConnector @Inject()(httpClient: HttpClient, appConfig: AppConfig)(implicit executionContext: ExecutionContext) {
  def matchSoleTraderDetails(soleTraderDetails: SoleTraderDetails)(implicit hc: HeaderCarrier): Future[SoleTraderVerificationResult] = {
    val jsonBody = Json.obj(
      "firstName" -> soleTraderDetails.firstName,
      "lastName" -> soleTraderDetails.lastName,
      "dateOfBirth" -> soleTraderDetails.dateOfBirth.format(ofPattern("uuuu-MM-dd")),
      "nino" -> soleTraderDetails.nino
    )

    httpClient.POST(appConfig.matchSoleTraderDetailsUrl, jsonBody)
  }
}
