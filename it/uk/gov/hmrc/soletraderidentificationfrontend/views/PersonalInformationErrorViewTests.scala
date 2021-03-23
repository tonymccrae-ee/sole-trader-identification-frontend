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

package uk.gov.hmrc.soletraderidentificationfrontend.views

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.libs.ws.WSResponse
import uk.gov.hmrc.soletraderidentificationfrontend.assets.MessageLookup.{ PersonalInformationError => messages}
import uk.gov.hmrc.soletraderidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.soletraderidentificationfrontend.utils.ViewSpecHelper.ElementExtensions

trait PersonalInformationErrorViewTests {
  this: ComponentSpecHelper =>

  def testPersonInformationErrorView(result: => WSResponse): Unit = {
    lazy val doc: Document = Jsoup.parse(result.body)

    "have the correct title" in {
      doc.title mustBe messages.title
    }

    "have the correct heading" in {
      doc.getH1Elements.get(0).text mustBe messages.heading
    }

    "have the correct first line" in {
      doc.getParagraphs.get(0).text mustBe messages.line_1
    }

    "have a try again button" in {
      doc.getSubmitButton.first.text mustBe messages.button
    }

  }

}
