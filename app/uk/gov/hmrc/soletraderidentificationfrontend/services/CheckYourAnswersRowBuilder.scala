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

package uk.gov.hmrc.soletraderidentificationfrontend.services

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{Actions, Key, SummaryListRow, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.ActionItem
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.soletraderidentificationfrontend.config.AppConfig
import uk.gov.hmrc.soletraderidentificationfrontend.controllers.routes
import uk.gov.hmrc.soletraderidentificationfrontend.models.{Address, IndividualDetails}
import uk.gov.hmrc.soletraderidentificationfrontend.utils.DateHelper.checkYourAnswersFormat

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class CheckYourAnswersRowBuilder @Inject()() {

  def buildSummaryListRowSeq(journeyId: String,
                             individualDetails: IndividualDetails,
                             optAddress: Option[Address],
                             enableSautrCheck: Boolean
                            )(implicit messages: Messages, config: AppConfig): Future[Seq[SummaryListRow]] = {

    val firstNameRow = SummaryListRow(
      key = Key(content = Text(messages("check-your-answers.first_name"))),
      value = Value(content = Text(individualDetails.firstName)),
      actions = Some(Actions(items = Seq(
        ActionItem(
          href = routes.CaptureFullNameController.show(journeyId).url,
          content = Text("Change"),
          visuallyHiddenText = Some(messages("check-your-answers.first_name"))
        )
      )))
    )

    val lastNameRow = SummaryListRow(
      key = Key(content = Text(messages("check-your-answers.last_name"))),
      value = Value(content = Text(individualDetails.lastName)),
      actions = Some(Actions(items = Seq(
        ActionItem(
          href = routes.CaptureFullNameController.show(journeyId).url,
          content = Text("Change"),
          visuallyHiddenText = Some(messages("check-your-answers.last_name"))
        )
      )))
    )

    val dobRow = SummaryListRow(
      key = Key(content = Text(messages("check-your-answers.dob"))),
      value = Value(content = Text(individualDetails.dateOfBirth.format(checkYourAnswersFormat))),
      actions = Some(Actions(items = Seq(
        ActionItem(
          href = routes.CaptureDateOfBirthController.show(journeyId).url,
          content = Text("Change"),
          visuallyHiddenText = Some(messages("check-your-answers.dob"))
        )
      )))
    )

    val ninoRow = SummaryListRow(
      key = Key(content = Text(messages("check-your-answers.nino"))),
      value = Value(content = Text(
        individualDetails.optNino match {
          case Some(nino) => nino.grouped(2).mkString(" ")
          case None => messages("check-your-answers.no_nino")
        }
      )),
      actions = Some(Actions(items = Seq(
        ActionItem(
          href = routes.CaptureNinoController.show(journeyId).url,
          content = Text("Change"),
          visuallyHiddenText = Some(messages("check-your-answers.nino"))
        )
      )))
    )

    val sautrRow = if (enableSautrCheck) Some(SummaryListRow(
      key = Key(content = Text(messages("check-your-answers.sautr"))),
      value = Value(content = Text(
        individualDetails.optSautr match {
          case Some(utr) => utr
          case None => messages("check-your-answers.no_sautr")
        }
      )),
      actions = Some(Actions(items = Seq(
        ActionItem(
          href = routes.CaptureSautrController.show(journeyId).url,
          content = Text("Change"),
          visuallyHiddenText = Some(messages("check-your-answers.sautr"))
        )
      )))
    )) else None

    def addressCheckYourAnswersFormat(countries: Seq[(String, String)], address: Address): String = {
      val countryName = countries.toMap.get(address.countryCode) match {
        case Some(countryName) => countryName
        case _ => throw new InternalServerException(s"Country code: ${address.countryCode} could not be found")
      }
      List(Some(address.line1), Some(address.line2), address.line3, address.line4, address.line5, address.postCode, Some(countryName)).flatten.mkString("<br>")
    }

    val addressRow = optAddress match {
      case Some(address) =>
        Seq(SummaryListRow(
          key = Key(content = Text(messages("check-your-answers.home_address"))),
          value = Value(HtmlContent(addressCheckYourAnswersFormat(config.countries, address))),
          actions = Some(Actions(items = Seq(
            ActionItem(
              href = routes.CaptureAddressController.show(journeyId).url,
              content = Text("Change"),
              visuallyHiddenText = Some(messages("check-your-answers.home_address"))
            )
          )))
        ))
      case _ => Seq.empty
    }

    Future.successful(Seq(firstNameRow, lastNameRow, dobRow, ninoRow) ++ addressRow ++ sautrRow)

  }

}


