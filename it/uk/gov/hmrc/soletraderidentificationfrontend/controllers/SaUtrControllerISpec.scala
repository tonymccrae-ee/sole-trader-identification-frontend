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

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.test.Helpers._
import uk.gov.hmrc.soletraderidentificationfrontend.assets.MessageLookup.{SaUtr => messages}
import uk.gov.hmrc.soletraderidentificationfrontend.utils.{ComponentSpecHelper, ViewSpec}

class SaUtrControllerISpec extends ComponentSpecHelper with ViewSpec {

  "GET /sa-utr" should {
    lazy val result = get("/sa-utr")
    lazy val doc: Document = Jsoup.parse(result.body)

    "return OK" in {
      result must have(httpStatus(OK))
    }

    "have a view with the correct title" in {
      doc.title mustBe messages.title
    }

    "have a view with the correct heading" in {
      doc.getH1Elements.text mustBe messages.heading
    }

    "have a view with the correct first line" in {
      doc.getParagraphs.get(0).text() mustBe messages.line_1
    }

    "have a view with correct details element" in {
      doc.getSpan("details-summary-text").text() mustBe messages.line_2
      doc.getParagraphs.get(1).text() mustBe messages.details_line_1
      doc.getParagraphs.get(2).text() mustBe messages.details_line_2
      doc.getParagraphs.get(3).text() mustBe messages.details_line_3
    }
  }

  "POST /sa-utr" should {
    lazy val result = post("/sa-utr")("")

    "redirect to Check Your Answers Page" in {
      result must have(
        httpStatus(SEE_OTHER),
        redirectUri(routes.CheckYourAnswersController.show().url)
      )
    }
  }
}
