/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.soletraderidentificationfrontend.assets

object MessageLookup {

  object Base {
    val confirmAndContinue = "Confirm and continue"
    val change = "Change"
    val saveAndContinue = "Save and continue"
    val saveAndComeBack = "Save and come back later"

    object Error {
      val title = "There is a problem"
      val error = "Error: "
    }
  }

  object CapturePersonalDetails {
    val title = "Enter your details"
    val heading = "Who do you want to register for VAT?"
    val line_1 = "We will attempt to match these details with the information we already have."
    val form_field_1 = "First name"
    val form_field_2 = "Last name"
    val form_field_3 = "Date of birth"
    val form_field_3_hint = "For example, 27 3 2007"
  }

  object CaptureNino {
    val title = "What is your National Insurance number?"
    val heading = "What is John Smith’s National Insurance number?"
    val line_1 = "It’s on the National Insurance card, benefit letter, payslip or P60. For example, ‘QQ 12 34 56 C’."
    val line_2 = "John Smith does not have a National Insurance number"
    val form_field_1 = "It’s on the National Insurance card, benefit letter, payslip or P60. For example, ‘QQ 12 34 56 C’."
    object Error {
      val invalidNinoEntered = "Enter a National Insurance number in the correct format"
    }
  }

  object CaptureSautr {
    val title = "What is John Smith’s Unique Taxpayer Reference?"
    val heading = "What is John Smith’s Unique Taxpayer Reference?"
    val line_1 = "This is 10 numbers, for example 1234567890. It will be on tax returns and other letters about Self Assessment. It may be called ‘reference’, ‘UTR’ or ‘official use’."
    val line_2 = "John Smith does not have a Unique Taxpayer Reference"
    val details_line_1 = "Your UTR helps us identify your business"
    val details_line_2 = "I cannot find the UTR"
    val details_line_3 = "The business does not have a UTR"

    object Error {
      val invalidSautrEntered = "Enter a Unique Taxpayer Reference in the correct format"
    }
  }

  object CheckYourAnswers {
    val title = "Check your answers"
    val heading = "Check your answers"
    val firstName = "First name"
    val lastName = "Last name"
    val dob = "Date of birth"
    val nino = "National insurance number"
    val sautr = "Unique taxpayers reference number"
    val noSautr = "The business does not have a UTR"
  }

}
