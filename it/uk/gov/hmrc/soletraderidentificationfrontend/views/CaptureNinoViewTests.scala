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
import uk.gov.hmrc.soletraderidentificationfrontend.assets.MessageLookup.{Base, Header, CaptureNino => messages}
import uk.gov.hmrc.soletraderidentificationfrontend.config.AppConfig
import uk.gov.hmrc.soletraderidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.soletraderidentificationfrontend.utils.ViewSpecHelper.ElementExtensions

trait CaptureNinoViewTests {
  this: ComponentSpecHelper =>

  def testCaptureNinoView(result: => WSResponse): Unit = {
    lazy val doc: Document = Jsoup.parse(result.body)
    lazy val config = app.injector.instanceOf[AppConfig]

    "have a sign out link in the header" in {
      doc.getSignOutText mustBe Header.signOut
    }

    "have sign out link redirecting to feedback page" in {
      doc.getSignOutLink mustBe config.vatRegFeedbackUrl
    }

    "have the correct title" in {
      doc.title mustBe messages.title
    }

    "have the correct heading" in {
      doc.getH1Elements.text mustBe messages.heading
    }

    "have the correct hint text" in {
      doc.getElementsByClass("govuk-hint").text mustBe messages.line_1
    }

    "have correct labels in the form" in {
      doc.getElementById("nino-hint").text mustBe messages.form_field_1
    }

    "have a save and continue button" in {
      doc.getSubmitButton.first.text mustBe Base.saveAndContinue
    }

  }

  def testCaptureNinoErrorMessages(result: => WSResponse): Unit = {
    lazy val doc: Document = Jsoup.parse(result.body)

    "correctly display the error summary" in {
      doc.getErrorSummaryTitle.text mustBe Base.Error.title
      doc.getErrorSummaryBody.text mustBe messages.Error.invalidNinoEntered
    }

    "correctly display the field errors" in {
      doc.getFieldErrorMessage.text mustBe Base.Error.error + messages.Error.invalidNinoEntered
    }
  }
}