/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.soletraderidentificationfrontend.controllers

import java.time.LocalDate
import java.util.UUID

import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import uk.gov.hmrc.soletraderidentificationfrontend.models.SoleTraderDetailsModel
import uk.gov.hmrc.soletraderidentificationfrontend.repositories.SoleTraderDetailsRepository
import uk.gov.hmrc.soletraderidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.soletraderidentificationfrontend.views.CheckYourAnswersViewTests

import scala.concurrent.ExecutionContext.Implicits.global


class CheckYourAnswersControllerISpec extends ComponentSpecHelper with CheckYourAnswersViewTests {

  "GET /check-your-answers-business" should {
    val testJourneyId = UUID.randomUUID().toString
    val testFirstName = "John"
    val testLastName = "Smith"
    val testSautr = "1234567890"
    val testNino = "AA111111A"

    val testSoleTraderDetails = SoleTraderDetailsModel(testFirstName,testLastName, LocalDate.parse("1978-01-05") , testNino,Some(testSautr))
    await(app.injector.instanceOf[SoleTraderDetailsRepository].insert(testSoleTraderDetails))
    lazy val result: WSResponse = get(s"/check-your-answers-business/$testJourneyId")

    "return OK" in {
      result.status mustBe OK
    }

    "return a view which" should {
      testCheckYourAnswersView(result, testJourneyId)
    }
  }

  "POST /check-your-answers-business" should {
    lazy val result = post("/check-your-answers-business")()

    "return NotImplemented" in {
      result.status mustBe NOT_IMPLEMENTED
    }
  }
}
