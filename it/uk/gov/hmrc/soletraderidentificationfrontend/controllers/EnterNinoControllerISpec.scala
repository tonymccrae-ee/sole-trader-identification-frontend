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
import uk.gov.hmrc.soletraderidentificationfrontend.assets.MessageLookup.{EnterNino => messages}
import uk.gov.hmrc.soletraderidentificationfrontend.utils.{ComponentSpecHelper, ViewSpec}

class EnterNinoControllerISpec extends ComponentSpecHelper with ViewSpec {

  "GET /national-insurance-number" should {
    lazy val result = get("/national-insurance-number")
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
      doc.getParagraphs.text() mustBe messages.line_2
    }

    "have a view with correct labels in the form" in {
      doc.getLabelElement.get(0).text() mustBe messages.form_field_1
    }
  }

  "POST /national-insurance-number" should {
    lazy val result = post("/national-insurance-number")("")

    "return NotImplemented" in {
      result must have(httpStatus(NOT_IMPLEMENTED))
    }
  }
}
