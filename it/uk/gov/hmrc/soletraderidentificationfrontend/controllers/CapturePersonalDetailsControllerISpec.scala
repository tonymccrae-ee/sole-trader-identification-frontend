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

import play.api.test.Helpers._
import uk.gov.hmrc.soletraderidentificationfrontend.models.PersonalDetailsModel
import uk.gov.hmrc.soletraderidentificationfrontend.repositories.SoleTraderDetailsRepository
import uk.gov.hmrc.soletraderidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.soletraderidentificationfrontend.views.CapturePersonalDetailsViewTests

class CapturePersonalDetailsControllerISpec extends ComponentSpecHelper with CapturePersonalDetailsViewTests {
  val testFirstName = "John"
  val testLastName = "Smith"
  val testDay = "01"
  val testMonth = "01"
  val testYear = "1990"

  val testJourneyId: String = "testJourneyId"

  "GET /personal-details" should {
    lazy val result = get(s"/personal-details/$testJourneyId")

    "return OK" in {
      result.status mustBe OK
    }

    "return a view which" should {
      testCapturePersonalDetailsView(result)
    }
  }

  "POST /personal-details" when {
    val testPersonalDetails = PersonalDetailsModel(testFirstName, testLastName, LocalDate.parse(s"$testYear-$testMonth-$testDay"))

    "the whole form is correctly formatted" should {
      "store the personal details in the database" in {
        post(s"/personal-details/$testJourneyId")(
          "first-name" -> testFirstName,
          "last-name" -> testLastName,
          "date-of-birth-day" -> testDay,
          "date-of-birth-month" -> testMonth,
          "date-of-birth-year" -> testYear
        )

        val optPersonalDetails = await(app.injector.instanceOf[SoleTraderDetailsRepository].retrievePersonalDetails(testJourneyId))

        optPersonalDetails mustBe Some(testPersonalDetails)
      }

      "redirect to the Capture Nino page" in {
        lazy val result = post(s"/personal-details/$testJourneyId")(
          "first-name" -> testFirstName,
          "last-name" -> testLastName,
          "date-of-birth-day" -> testDay,
          "date-of-birth-month" -> testMonth,
          "date-of-birth-year" -> testYear
        )
        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CaptureNinoController.show(testJourneyId).url)
        )
      }
    }

    "the whole form is missing" should {
      lazy val result = post(s"/personal-details/$testJourneyId")(
        "first-name" -> "",
        "last-name" -> "",
        "date-of-birth-day" -> "",
        "date-of-birth-month" -> "",
        "date-of-birth-year" -> ""
      )
      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }
      testCapturePersonalDetailsErrorMessage(result)
    }

    "the first name is missing" should {
      lazy val result = post(s"/personal-details/$testJourneyId")(
        "first-name" -> "",
        "last-name" -> testLastName,
        "date-of-birth-day" -> testDay,
        "date-of-birth-month" -> testMonth,
        "date-of-birth-year" -> testYear
      )
      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }
      testCapturePersonalDetailsErrorMessageNoFirstName(result)
    }

    "the last name is missing" should {
      lazy val result = post(s"/personal-details/$testJourneyId")(
        "first-name" -> testFirstName,
        "last-name" -> "",
        "date-of-birth-day" -> testDay,
        "date-of-birth-month" -> testMonth,
        "date-of-birth-year" -> testYear
      )
      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }
      testCapturePersonalDetailsErrorMessageNoLastName(result)
    }

    "the date of birth is missing" should {
      lazy val result = post(s"/personal-details/$testJourneyId")(
        "first-name" -> testFirstName,
        "last-name" -> testLastName,
        "date-of-birth-day" -> "",
        "date-of-birth-month" -> "",
        "date-of-birth-year" -> ""
      )
      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }
      testCapturePersonalDetailsErrorMessageNoDob(result)
    }

    "the first name and last name are missing" should {
      lazy val result = post(s"/personal-details/$testJourneyId")(
        "first-name" -> "",
        "last-name" -> "",
        "date-of-birth-day" -> testDay,
        "date-of-birth-month" -> testMonth,
        "date-of-birth-year" -> testYear
      )
      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }
      testCapturePersonalDetailsErrorMessageNoFirstNameAndLastName(result)
    }

    "the first name and dob are missing" should {
      lazy val result = post(s"/personal-details/$testJourneyId")(
        "first-name" -> "",
        "last-name" -> testLastName,
        "date-of-birth-day" -> "",
        "date-of-birth-month" -> "",
        "date-of-birth-year" -> ""
      )
      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }
      testCapturePersonalDetailsErrorMessageNoFirstNameAndDob(result)
    }

    "the last name and dob are missing" should {
      lazy val result = post(s"/personal-details/$testJourneyId")(
        "first-name" -> testFirstName,
        "last-name" -> "",
        "date-of-birth-day" -> "",
        "date-of-birth-month" -> "",
        "date-of-birth-year" -> ""
      )
      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }
      testCapturePersonalDetailsErrorMessageNoLastNameAndDob(result)
    }

    "the day in dob is missing" should {
      lazy val result = post(s"/personal-details/$testJourneyId")(
        "first-name" -> testFirstName,
        "last-name" -> testLastName,
        "date-of-birth-day" -> "",
        "date-of-birth-month" -> testMonth,
        "date-of-birth-year" -> testYear
      )
      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }
      testCapturePersonalDetailsErrorMessageNoDay(result)
    }

    "the month in dob is missing" should {
      lazy val result = post(s"/personal-details/$testJourneyId")(
        "first-name" -> testFirstName,
        "last-name" -> testLastName,
        "date-of-birth-day" -> testDay,
        "date-of-birth-month" -> "",
        "date-of-birth-year" -> testYear
      )
      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }
      testCapturePersonalDetailsErrorMessageNoMonth(result)
    }

    "the year in dob is missing" should {
      lazy val result = post(s"/personal-details/$testJourneyId")(
        "first-name" -> testFirstName,
        "last-name" -> testLastName,
        "date-of-birth-day" -> testDay,
        "date-of-birth-month" -> testMonth,
        "date-of-birth-year" -> ""
      )
      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }
      testCapturePersonalDetailsErrorMessageNoYear(result)
    }

    "the day and month in dob are missing" should {
      lazy val result = post(s"/personal-details/$testJourneyId")(
        "first-name" -> testFirstName,
        "last-name" -> testLastName,
        "date-of-birth-day" -> "",
        "date-of-birth-month" -> "",
        "date-of-birth-year" -> testYear
      )
      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }
      testCapturePersonalDetailsErrorMessageNoDayNoMonth(result)
    }

    "the day and year in dob are missing" should {
      lazy val result = post(s"/personal-details/$testJourneyId")(
        "first-name" -> testFirstName,
        "last-name" -> testLastName,
        "date-of-birth-day" -> "",
        "date-of-birth-month" -> testMonth,
        "date-of-birth-year" -> ""
      )
      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }
      testCapturePersonalDetailsErrorMessageNoDayNoYear(result)
    }

    "the month and year in dob are missing" should {
      lazy val result = post(s"/personal-details/$testJourneyId")(
        "first-name" -> testFirstName,
        "last-name" -> testLastName,
        "date-of-birth-day" -> testDay,
        "date-of-birth-month" -> "",
        "date-of-birth-year" -> ""
      )
      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }
      testCapturePersonalDetailsErrorMessageNoMonthNoYear(result)
    }

    "an invalid day is submitted" should {
      lazy val result = post(s"/personal-details/$testJourneyId")(
        "first-name" -> testFirstName,
        "last-name" -> testLastName,
        "date-of-birth-day" -> "35",
        "date-of-birth-month" -> testMonth,
        "date-of-birth-year" -> testYear
      )
      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }
      testCapturePersonalDetailsErrorMessageInvalidDay(result)
    }

    "an invalid month is submitted" should {
      lazy val result = post(s"/personal-details/$testJourneyId")(
        "first-name" -> testFirstName,
        "last-name" -> testLastName,
        "date-of-birth-day" -> testDay,
        "date-of-birth-month" -> "15",
        "date-of-birth-year" -> testYear
      )
      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }
      testCapturePersonalDetailsErrorMessageInvalidMonth(result)
    }

    "a future year is submitted" should {
      lazy val result = post(s"/personal-details/$testJourneyId")(
        "first-name" -> testFirstName,
        "last-name" -> testLastName,
        "date-of-birth-day" -> testDay,
        "date-of-birth-month" -> testMonth,
        "date-of-birth-year" -> "2024"
      )
      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }
      testCapturePersonalDetailsErrorMessageInvalidYear(result)
    }

    "the dob submitted is less than 16 years ago" should {
      lazy val result = post(s"/personal-details/$testJourneyId")(
        "first-name" -> testFirstName,
        "last-name" -> testLastName,
        "date-of-birth-day" -> testDay,
        "date-of-birth-month" -> testMonth,
        "date-of-birth-year" -> LocalDate.now.minusYears(10).getYear.toString
      )
      "return a bad request" in {
        result.status mustBe BAD_REQUEST
      }
      testCapturePersonalDetailsErrorMessageInvalidAge(result)
    }
  }
}

