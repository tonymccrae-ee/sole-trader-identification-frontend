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

package uk.gov.hmrc.soletraderidentificationfrontend.views

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.{MustMatchers, WordSpecLike}
import play.api.libs.ws.WSResponse
import uk.gov.hmrc.soletraderidentificationfrontend.assets.MessageLookup.{Base, CheckYourAnswers => messages}
import uk.gov.hmrc.soletraderidentificationfrontend.controllers.routes
import uk.gov.hmrc.soletraderidentificationfrontend.utils.ViewSpecHelper.ElementExtensions

import scala.collection.JavaConversions._


trait CheckYourAnswersViewTests {
  this: WordSpecLike with MustMatchers =>

  def testCheckYourAnswersView(result: => WSResponse): Unit = {
    lazy val doc: Document = Jsoup.parse(result.body)

    "have the correct title" in {
      doc.title mustBe messages.title
    }

    "have the correct heading" in {
      doc.getH1Elements.text mustBe messages.heading
    }

    "have a summary list which" should {
      lazy val summaryListRows = doc.getSummaryListRows.iterator().toList

      "have 5 rows" in {
        summaryListRows.size mustBe 5
      }

      "have a first name row" in {
        val firstNameRow = summaryListRows.head

        firstNameRow.getSummaryListQuestion mustBe messages.firstName
        firstNameRow.getSummaryListAnswer mustBe "John"
        firstNameRow.getSummaryListChangeLink mustBe routes.CapturePersonalDetailsController.show().url
        firstNameRow.getSummaryListChangeText mustBe s"${Base.change} ${messages.firstName}"
      }

      "have a last name row" in {
        val lastNameRow = summaryListRows(1)

        lastNameRow.getSummaryListQuestion mustBe messages.lastName
        lastNameRow.getSummaryListAnswer mustBe "Smith"
        lastNameRow.getSummaryListChangeLink mustBe routes.CapturePersonalDetailsController.show().url
        lastNameRow.getSummaryListChangeText mustBe s"${Base.change} ${messages.lastName}"
      }

      "have a date of birth row" in {
        val dateOfBirthRow = summaryListRows(2)

        dateOfBirthRow.getSummaryListQuestion mustBe messages.dob
        dateOfBirthRow.getSummaryListAnswer mustBe "5 January 1978"
        dateOfBirthRow.getSummaryListChangeLink mustBe routes.CapturePersonalDetailsController.show().url
        dateOfBirthRow.getSummaryListChangeText mustBe s"${Base.change} ${messages.dob}"
      }

      "have a nino row" in {
        val ninoRow = summaryListRows(3)

        ninoRow.getSummaryListQuestion mustBe messages.nino
        ninoRow.getSummaryListAnswer mustBe "AA 11 11 11 A"
        ninoRow.getSummaryListChangeLink mustBe routes.CaptureNinoController.show("").url
        ninoRow.getSummaryListChangeText mustBe s"${Base.change} ${messages.nino}"
      }

      "have an sautr row" in {
        val sautrRow = summaryListRows.last

        sautrRow.getSummaryListQuestion mustBe messages.sautr
        sautrRow.getSummaryListAnswer mustBe "1234567890"
        sautrRow.getSummaryListChangeLink mustBe routes.CaptureSautrController.show().url
        sautrRow.getSummaryListChangeText mustBe s"${Base.change} ${messages.sautr}"
      }

      "have a continue and confirm button" in {
        doc.getSubmitButton.text mustBe Base.confirmAndContinue
      }
    }

  }

}
