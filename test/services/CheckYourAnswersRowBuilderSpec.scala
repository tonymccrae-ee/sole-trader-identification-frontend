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

package services

import helpers.TestConstants._
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.govukfrontend.views.Aliases.{Actions, Key, SummaryListRow, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.ActionItem
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.soletraderidentificationfrontend.config.AppConfig
import uk.gov.hmrc.soletraderidentificationfrontend.controllers.routes
import uk.gov.hmrc.soletraderidentificationfrontend.models.Address
import uk.gov.hmrc.soletraderidentificationfrontend.services.CheckYourAnswersRowBuilder
import uk.gov.hmrc.soletraderidentificationfrontend.utils.DateHelper.checkYourAnswersFormat

class CheckYourAnswersRowBuilderSpec extends AnyWordSpec with Matchers with MockitoSugar with GuiceOneAppPerSuite {

  object TestService extends CheckYourAnswersRowBuilder()

  implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

  implicit val mockMessages: Messages = app.injector.instanceOf[MessagesApi].preferred(request)

  implicit val mockAppConfig: AppConfig = app.injector.instanceOf[AppConfig]

  val testFirstNameRow = SummaryListRow(
    key = Key(content = Text("First name")),
    value = Value(content = Text(testFirstName)),
    actions = Some(Actions(items = Seq(
      ActionItem(
        href = routes.CaptureFullNameController.show(testJourneyId).url,
        content = Text("Change"),
        visuallyHiddenText = Some("First name")
      )
    )))
  )

  val testLastNameRow = SummaryListRow(
    key = Key(content = Text("Last name")),
    value = Value(content = Text(testLastName)),
    actions = Some(Actions(items = Seq(
      ActionItem(
        href = routes.CaptureFullNameController.show(testJourneyId).url,
        content = Text("Change"),
        visuallyHiddenText = Some("Last name")
      )
    )))
  )

  val testDateOfBirthRow = SummaryListRow(
    key = Key(content = Text("Date of birth")),
    value = Value(content = Text(testDateOfBirth.format(checkYourAnswersFormat))),
    actions = Some(Actions(items = Seq(
      ActionItem(
        href = routes.CaptureDateOfBirthController.show(testJourneyId).url,
        content = Text("Change"),
        visuallyHiddenText = Some("Date of birth")
      )
    )))
  )

  val testNinoRow = SummaryListRow(
    key = Key(content = Text("National insurance number")),
    value = Value(content = Text(testNino.grouped(2).mkString(" "))),
    actions = Some(Actions(items = Seq(
      ActionItem(
        href = routes.CaptureNinoController.show(testJourneyId).url,
        content = Text("Change"),
        visuallyHiddenText = Some("National insurance number")
      )
    )))
  )

  val testNoNinoRow = SummaryListRow(
    key = Key(content = Text("National insurance number")),
    value = Value(content = Text("I do not have a National Insurance number")),
    actions = Some(Actions(items = Seq(
      ActionItem(
        href = routes.CaptureNinoController.show(testJourneyId).url,
        content = Text("Change"),
        visuallyHiddenText = Some("National insurance number")
      )
    )))
  )

  val testSautrRow = SummaryListRow(
    key = Key(content = Text("Unique taxpayers reference number")),
    value = Value(content = Text(testSautr)),
    actions = Some(Actions(items = Seq(
      ActionItem(
        href = routes.CaptureSautrController.show(testJourneyId).url,
        content = Text("Change"),
        visuallyHiddenText = Some("Unique taxpayers reference number")
      )
    )))
  )

  val testNoSautrRow = SummaryListRow(
    key = Key(content = Text("Unique taxpayers reference number")),
    value = Value(content = Text("The business does not have a UTR")),
    actions = Some(Actions(items = Seq(
      ActionItem(
        href = routes.CaptureSautrController.show(testJourneyId).url,
        content = Text("Change"),
        visuallyHiddenText = Some("Unique taxpayers reference number")
      )
    )))
  )

  def addressCheckYourAnswersFormat(countries: Seq[(String, String)], address: Address): String = {
    val countryName = countries.toMap.get(address.countryCode) match {
      case Some(countryName) => countryName
      case _ => throw new InternalServerException("")
    }
    List(Some(address.line1), Some(address.line2), address.line3, address.line4, address.line5, address.postCode, Some(countryName)).flatten.mkString("<br>")
  }

  val testAddressRow = SummaryListRow(
    key = Key(content = Text("Home Address")),
    value = Value(content = HtmlContent(addressCheckYourAnswersFormat(mockAppConfig.countries, testAddress))),
    actions = Some(Actions(items = Seq(
      ActionItem(
        href = routes.CaptureAddressController.show(testJourneyId).url,
        content = Text("Change"),
        visuallyHiddenText = Some("Home Address")
      )
    )))
  )

  "buildSummaryListRowSeq" should {
    "build a summary list sequence" when {
      "the user is on the individual journey" when {
        "there is a nino" in {

          val result = await(TestService.buildSummaryListRowSeq(testJourneyId, testIndividualDetailsNoSautr, optAddress = None, enableSautrCheck = false))

          result mustBe Seq(testFirstNameRow, testLastNameRow, testDateOfBirthRow, testNinoRow)
        }
      }

      "the user is on the sole trader journey" when {
        "there is a nino and sautr but no address provided" in {
          val result = await(TestService.buildSummaryListRowSeq(testJourneyId, testIndividualDetails, optAddress = None, enableSautrCheck = true))

          result mustBe Seq(testFirstNameRow, testLastNameRow, testDateOfBirthRow, testNinoRow, testSautrRow)
        }

        "the nino has not been provided but sautr and address have" in {
          val result = await(TestService.buildSummaryListRowSeq(testJourneyId, testIndividualDetailsNoNino, Some(testAddress), enableSautrCheck = true))

          result mustBe Seq(testFirstNameRow, testLastNameRow, testDateOfBirthRow, testNoNinoRow, testAddressRow, testSautrRow)
        }

        "the nino and sautr have not been provided but an address has" in {
          val result = await(TestService.buildSummaryListRowSeq(testJourneyId, testIndividualDetailsNoNinoNoSautr, Some(testAddress), enableSautrCheck = true))

          result mustBe Seq(testFirstNameRow, testLastNameRow, testDateOfBirthRow, testNoNinoRow, testAddressRow, testNoSautrRow)
        }
      }
    }
  }

}
