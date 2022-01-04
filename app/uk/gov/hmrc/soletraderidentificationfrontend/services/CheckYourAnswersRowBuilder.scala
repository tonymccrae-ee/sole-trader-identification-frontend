/*
 * Copyright 2022 HM Revenue & Customs
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
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.Aliases.{Actions, Key, SummaryListRow, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.ActionItem
import uk.gov.hmrc.soletraderidentificationfrontend.config.AppConfig
import uk.gov.hmrc.soletraderidentificationfrontend.controllers.routes
import uk.gov.hmrc.soletraderidentificationfrontend.models.{Address, IndividualDetails, Overseas}
import uk.gov.hmrc.soletraderidentificationfrontend.utils.DateHelper.checkYourAnswersFormat

import javax.inject.{Inject, Singleton}

@Singleton
class CheckYourAnswersRowBuilder @Inject()() {

  def buildSummaryListRows(journeyId: String,
                           individualDetails: IndividualDetails,
                           optAddress: Option[Address],
                           optSaPostcode: Option[String],
                           optOverseasTaxId: Option[Overseas],
                           enableSautrCheck: Boolean
                          )(implicit messages: Messages, config: AppConfig): Seq[SummaryListRow] = {

    val firstNameRow = buildSummaryRow(
      messages("check-your-answers.first_name"),
      individualDetails.firstName,
      routes.CaptureFullNameController.show(journeyId)
    )

    val lastNameRow = buildSummaryRow(
      messages("check-your-answers.last_name"),
      individualDetails.lastName,
      routes.CaptureFullNameController.show(journeyId)
    )

    val dateOfBirthRow = buildSummaryRow(
      messages("check-your-answers.dob"),
      individualDetails.dateOfBirth.format(checkYourAnswersFormat),
      routes.CaptureDateOfBirthController.show(journeyId)
    )

    val ninoRow = buildSummaryRow(
      messages("check-your-answers.nino"),
      individualDetails.optNino match {
        case Some(nino) => nino.grouped(2).mkString(" ")
        case None => messages("check-your-answers.no_nino")
      },
      routes.CaptureNinoController.show(journeyId)
    )

    val sautrRow = if (enableSautrCheck) {
      Some(buildSummaryRow(
        messages("check-your-answers.sautr"),
        individualDetails.optSautr match {
          case Some(utr) => utr
          case None => messages("check-your-answers.no_sautr")
        },
        routes.CaptureSautrController.show(journeyId)
      ))
    } else {
      None
    }

    val saPostcodeRow = if (individualDetails.optSautr.isDefined && individualDetails.optNino.isEmpty) {
      Some(buildSummaryRow(
        messages("check-your-answers.sa_postcode"),
        optSaPostcode match {
          case Some(saPostcode) => saPostcode
          case None => messages("check-your-answers.no_sa_postcode")
        },
        routes.CaptureSaPostcodeController.show(journeyId)
      ))
    } else {
      None
    }

    val overseasIdentifiersRow = if (individualDetails.optNino.isEmpty && enableSautrCheck) {
      Some(buildSummaryRow(
        messages("check-your-answers.tax_identifiers"),
        optOverseasTaxId match {
          case Some(overseasTaxId) => Seq(overseasTaxId.taxIdentifier, config.getCountryName(overseasTaxId.country)).mkString("<br>")
          case None => messages("check-your-answers.no_tax-identifiers")
        },
        routes.CaptureOverseasTaxIdentifiersController.show(journeyId)
      ))
    } else {
      None
    }

    val addressRow = optAddress.map {
      address =>
        val formattedAddress = Seq(
          Some(address.line1),
          Some(address.line2),
          address.line3,
          address.line4,
          address.line5,
          address.postcode,
          Some(config.getCountryName(address.countryCode))
        ).flatten.mkString("<br>")

        buildSummaryRow(
          messages("check-your-answers.home_address"),
          formattedAddress,
          routes.CaptureAddressController.show(journeyId)
        )
    }

    Seq(firstNameRow, lastNameRow, dateOfBirthRow, ninoRow) ++ addressRow ++ sautrRow ++ saPostcodeRow ++ overseasIdentifiersRow

  }

  private def buildSummaryRow(key: String, value: String, changeLink: Call) = SummaryListRow(
    key = Key(content = Text(key)),
    value = Value(HtmlContent(value)),
    actions = Some(Actions(items = Seq(
      ActionItem(
        href = changeLink.url,
        content = Text("Change"),
        visuallyHiddenText = Some(key)
      )
    )))
  )
}


