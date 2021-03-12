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
import uk.gov.hmrc.soletraderidentificationfrontend.assets.TestConstants._
import uk.gov.hmrc.soletraderidentificationfrontend.stubs.SoleTraderIdentificationStub
import uk.gov.hmrc.soletraderidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.soletraderidentificationfrontend.views.CaptureDateOfBirthViewTests

import java.time.LocalDate

class CaptureDateOfBirthControllerISpec extends ComponentSpecHelper with CaptureDateOfBirthViewTests with SoleTraderIdentificationStub {

  "GET /date-of-birth" should {
    lazy val result = get(s"/date-of-birth/$testJourneyId")

    "return OK" in {
      result.status mustBe OK
    }

    "return a view which" should {
      testCaptureDateOfBirthView(result)
    }
  }

  "POST /date-of-birth" when {
    "the whole form is correctly formatted" should {
      "redirect to the Capture Nino page and store the data in the backend" in {
        stubStoreDob(testJourneyId, testDateOfBirth)(status = OK)

        lazy val result = post(s"/date-of-birth/$testJourneyId")(
          "date-of-birth-day" -> testDateOfBirth.getDayOfMonth.toString,
          "date-of-birth-month" -> testDateOfBirth.getMonthValue.toString,
          "date-of-birth-year" -> testDateOfBirth.getYear.toString
        )

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CaptureNinoController.show(testJourneyId).url)
        )
      }
    }

    "the whole form is missing" should {
      lazy val result = post(s"/date-of-birth/$testJourneyId")(
        "date-of-birth-day" -> "",
        "date-of-birth-month" -> "",
        "date-of-birth-year" -> ""
      )
      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }
      testCaptureDateOfBirthErrorMessage(result)
    }

    "the date of birth is missing" should {
      lazy val result = post(s"/date-of-birth/$testJourneyId")(
        "date-of-birth-day" -> "",
        "date-of-birth-month" -> "",
        "date-of-birth-year" -> ""
      )
      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }
      testCaptureDateOfBirthErrorMessageNoDob(result)
    }

    "the day in dob is missing" should {
      lazy val result = post(s"/date-of-birth/$testJourneyId")(
        "date-of-birth-day" -> "",
        "date-of-birth-month" -> testDateOfBirth.getMonthValue.toString,
        "date-of-birth-year" -> testDateOfBirth.getYear.toString
      )
      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }
      testCaptureDateOfBirthErrorMessageNoDay(result)
    }

    "the month in dob is missing" should {
      lazy val result = post(s"/date-of-birth/$testJourneyId")(
        "date-of-birth-day" -> testDateOfBirth.getDayOfMonth.toString,
        "date-of-birth-month" -> "",
        "date-of-birth-year" -> testDateOfBirth.getYear.toString
      )
      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }
      testCaptureDateOfBirthErrorMessageNoMonth(result)
    }

    "the year in dob is missing" should {
      lazy val result = post(s"/date-of-birth/$testJourneyId")(
        "date-of-birth-day" -> testDateOfBirth.getDayOfMonth.toString,
        "date-of-birth-month" -> testDateOfBirth.getMonthValue.toString,
        "date-of-birth-year" -> ""
      )
      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }
      testCaptureDateOfBirthErrorMessageNoYear(result)
    }

    "the day and month in dob are missing" should {
      lazy val result = post(s"/date-of-birth/$testJourneyId")(
        "date-of-birth-day" -> "",
        "date-of-birth-month" -> "",
        "date-of-birth-year" -> testDateOfBirth.getYear.toString
      )
      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }
      testCaptureDateOfBirthErrorMessageNoDayNoMonth(result)
    }

    "the day and year in dob are missing" should {
      lazy val result = post(s"/date-of-birth/$testJourneyId")(
        "date-of-birth-day" -> "",
        "date-of-birth-month" -> testDateOfBirth.getMonthValue.toString,
        "date-of-birth-year" -> ""
      )
      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }
      testCaptureDateOfBirthErrorMessageNoDayNoYear(result)
    }

    "the month and year in dob are missing" should {
      lazy val result = post(s"/date-of-birth/$testJourneyId")(
        "date-of-birth-day" -> testDateOfBirth.getDayOfMonth.toString,
        "date-of-birth-month" -> "",
        "date-of-birth-year" -> ""
      )
      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }
      testCaptureDateOfBirthErrorMessageNoMonthNoYear(result)
    }

    "an invalid day is submitted" should {
      lazy val result = post(s"/date-of-birth/$testJourneyId")(
        "date-of-birth-day" -> "35",
        "date-of-birth-month" -> testDateOfBirth.getMonthValue.toString,
        "date-of-birth-year" -> testDateOfBirth.getYear.toString
      )
      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }
      testCaptureDateOfBirthErrorMessageInvalidDay(result)
    }

    "an invalid month is submitted" should {
      lazy val result = post(s"/date-of-birth/$testJourneyId")(
        "date-of-birth-day" -> testDateOfBirth.getDayOfMonth.toString,
        "date-of-birth-month" -> "15",
        "date-of-birth-year" -> testDateOfBirth.getYear.toString
      )
      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }
      testCaptureDateOfBirthErrorMessageInvalidMonth(result)
    }

    "a future year is submitted" should {
      lazy val result = post(s"/date-of-birth/$testJourneyId")(
        "date-of-birth-day" -> testDateOfBirth.getDayOfMonth.toString,
        "date-of-birth-month" -> testDateOfBirth.getMonthValue.toString,
        "date-of-birth-year" -> "2024"
      )
      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }
      testCaptureDateOfBirthErrorMessageInvalidYear(result)
    }

    "the dob submitted is less than 16 years ago" should {
      lazy val result = post(s"/date-of-birth/$testJourneyId")(
        "date-of-birth-day" -> testDateOfBirth.getDayOfMonth.toString,
        "date-of-birth-month" -> testDateOfBirth.getMonthValue.toString,
        "date-of-birth-year" -> LocalDate.now.minusYears(10).getYear.toString
      )
      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }
      testCaptureDateOfBirthErrorMessageInvalidAge(result)
    }
  }
}



