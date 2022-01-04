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
import uk.gov.hmrc.soletraderidentificationfrontend.assets.MessageLookup.{Base, BetaBanner, Header, CaptureNino => messages}
import uk.gov.hmrc.soletraderidentificationfrontend.assets.TestConstants.testSignOutUrl
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

    "have sign out link redirecting to signOutUrl from journey config" in {
      doc.getSignOutLink mustBe testSignOutUrl
    }

    "have the correct beta banner" in {
      doc.getBanner.text mustBe BetaBanner.title
      doc.getBannerLink mustBe config.betaFeedbackUrl("vrs")
    }

    "have a back link" in {
      doc.getBackLinkText mustBe Base.back
    }

    "have the correct title" in {
      doc.title mustBe messages.titleWithFirstName
    }

    "have the correct heading" in {
      doc.getH1Elements.text mustBe messages.headingWithFirstName
    }

    "have the correct hint text" in {
      doc.getElementsByClass("govuk-hint").text mustBe messages.line_1
    }

    "have correct labels in the form" in {
      doc.getElementById("nino-hint").text mustBe messages.form_field_1
    }

    "not have a link to skip nino question" in {
      doc.getElementsContainingText(messages.no_nino).toArray mustBe empty
    }

    "have a save and continue button" in {
      doc.getSubmitButton.first.text mustBe Base.saveAndContinue
    }

    "have a link to contact frontend" in {
      doc.getLink("get-help").text mustBe Base.getHelp
    }
  }

  def testNoNinoCaptureNinoView(result: => WSResponse): Unit = {
    lazy val doc: Document = Jsoup.parse(result.body)
    lazy val config = app.injector.instanceOf[AppConfig]


    "have a sign out link in the header" in {
      doc.getSignOutText mustBe Header.signOut
    }

    "have sign out link redirecting to signOutUrl from journey config" in {
      doc.getSignOutLink mustBe testSignOutUrl
    }

    "have the correct beta banner" in {
      doc.getBanner.text mustBe BetaBanner.title
    }

    "have a banner link that redirects to beta feedback" in {
      doc.getBannerLink mustBe config.betaFeedbackUrl("vrs")
    }

    "have the correct title" in {
      doc.title mustBe messages.titleWithFirstName
    }

    "have the correct heading" in {
      doc.getH1Elements.text mustBe messages.headingWithFirstName
    }

    "have the correct hint text" in {
      doc.getElementsByClass("govuk-hint").text mustBe messages.line_1
    }

    "have correct labels in the form" in {
      doc.getElementById("nino-hint").text mustBe messages.form_field_1
    }

    "have a correct link to skip nino question" in {
      doc.getElementById("no-nino").text() mustBe messages.no_nino
    }

    "have a save and continue button" in {
      doc.getSubmitButton.first.text mustBe Base.saveAndContinue
    }

    "have a link to contact frontend" in {
      doc.getLink("get-help").text mustBe Base.getHelp
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

  def testTitleAndHeadingInTheErrorView(result: => WSResponse): Unit = {
    lazy val doc: Document = Jsoup.parse(result.body)

    "have a correct custom title" in {
      doc.title mustBe Base.Error.error + messages.titleWithFirstName
    }

    "have a correct custom heading" in {
      doc.getH1Elements.get(0).text mustBe messages.headingWithFirstName
    }

  }

  def testTitleAndHeadingGivenNoCustomerFullName(result: => WSResponse): Unit = {
    lazy val doc: Document = Jsoup.parse(result.body)

    "have a correct custom title" in {
      doc.title mustBe Base.technicalDifficultiesTitle
    }

    "have a correct custom heading" in {
      doc.getH1Elements.get(0).text mustBe Base.technicalDifficultiesHeading
    }

  }
}
