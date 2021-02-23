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

package uk.gov.hmrc.soletraderidentificationfrontend.controllers

import play.api.test.Helpers._
import uk.gov.hmrc.soletraderidentificationfrontend.models.FullNameModel
import uk.gov.hmrc.soletraderidentificationfrontend.repositories.SoleTraderDetailsRepository
import uk.gov.hmrc.soletraderidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.soletraderidentificationfrontend.views.CaptureFullNameViewTests

class CaptureFullNameControllerISpec extends ComponentSpecHelper with CaptureFullNameViewTests {

  val testFirstName = "John"
  val testLastName = "Smith"

  val testJourneyId: String = "testJourneyId"

  "GET /full-name" should {
    lazy val result = get(s"/full-name/$testJourneyId")

    "return OK" in {
      result.status mustBe OK
    }

    "return a view which" should {
      testCaptureFullNameView(result)
    }
  }

  "POST /full-name" when {
    val testFullName = FullNameModel(testFirstName, testLastName)

    "the whole form is correctly formatted" should {
      "store the full name in the database" in {
        post(s"/full-name/$testJourneyId")(
          "first-name" -> testFirstName,
          "last-name" -> testLastName
        )

        val optFullName = await(app.injector.instanceOf[SoleTraderDetailsRepository].retrieveFullName(testJourneyId))

        optFullName mustBe Some(testFullName)
      }

      "redirect to the Capture Date of Birth page" in {
        lazy val result = post(s"/full-name/$testJourneyId")(
          "first-name" -> testFirstName,
          "last-name" -> testLastName
        )
        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CaptureDateOfBirthController.show(testJourneyId).url)
        )
      }
    }

    "the whole form is missing" should {
      lazy val result = post(s"/full-name/$testJourneyId")(
        "first-name" -> "",
        "last-name" -> ""
      )
      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }
      testCaptureFullNameErrorMessage(result)
    }

    "the first name is missing" should {
      lazy val result = post(s"/full-name/$testJourneyId")(
        "first-name" -> "",
        "last-name" -> testLastName
      )
      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }
      testCaptureFullNameErrorMessageNoFirstName(result)
    }

    "the last name is missing" should {
      lazy val result = post(s"/full-name/$testJourneyId")(
        "first-name" -> testFirstName,
        "last-name" -> ""
      )
      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }
      testCaptureFullNameErrorMessageNoLastName(result)
    }

    "the first name and last name are missing" should {
      lazy val result = post(s"/full-name/$testJourneyId")(
        "first-name" -> "",
        "last-name" -> ""
      )
      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }
      testCaptureFullNameErrorMessageNoFirstNameAndLastName(result)
    }
  }
}
