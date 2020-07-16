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

package uk.gov.hmrc.soletraderidentificationfrontend.forms

import java.time.LocalDate

import javax.inject.Inject
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraint
import play.api.i18n.Messages
import uk.gov.hmrc.soletraderidentificationfrontend.forms.mappings.Mappings
import uk.gov.hmrc.soletraderidentificationfrontend.forms.utils.ConstraintUtil._
import uk.gov.hmrc.soletraderidentificationfrontend.forms.utils.MappingUtil._
import uk.gov.hmrc.soletraderidentificationfrontend.forms.utils.TimeMachine
import uk.gov.hmrc.soletraderidentificationfrontend.forms.utils.ValidationHelper._
import uk.gov.hmrc.soletraderidentificationfrontend.models.PersonalDetailsModel

class CapturePersonalDetailsForm @Inject()(timeMachine: TimeMachine) extends Mappings {

  val dateRegex = "^[0-9]+$"

  def validName(text: String): Boolean = text.length > 0 && text.length < 100

  private val firstNameNotEntered: Constraint[String] = Constraint("first_name.not_entered")(
    firstName => validate(
      constraint = firstName.isEmpty,
      errMsg = "error.no_entry_first_name"
    )
  )

  private val firstNameInvalid: Constraint[String] = Constraint("first_name.invalid")(
    firstName => validateNot(
      constraint = validName(firstName.trim),
      errMsg = "error.invalid_first_name"
    )
  )

  private val lastNameNotEntered: Constraint[String] = Constraint("last_name.not_entered")(
    lastName => validate(
      constraint = lastName.isEmpty,
      errMsg = "error.no_entry_last_name"
    )
  )

  private val lastNameInvalid: Constraint[String] = Constraint("last_name.invalid")(
    lastName => validateNot(
      constraint = validName(lastName.trim),
      errMsg = "error.invalid_last_name"
    )
  )

  private val dobYearInvalid: Constraint[LocalDate] = constraint[LocalDate] {
    def now: LocalDate = timeMachine.now()

    dateOfBirth =>
      validateNot(
        constraint = dateOfBirth.isBefore(now),
        errMsg = "error.invalid_dob_year"
      )
  }

  private val invalidAge: Constraint[LocalDate] = constraint[LocalDate] {
    def now: LocalDate = timeMachine.now()

    val minAge = 16

    dateOfBirth =>
      validateNot(
        constraint = dateOfBirth.isBefore(now.minusYears(minAge)),
        errMsg = "error.invalid_age"
      )
  }

  def apply()(implicit messages: Messages): Form[PersonalDetailsModel] = {
    Form(
      mapping(
        "first-name" -> optText.toText.verifying(firstNameNotEntered andThen firstNameInvalid),
        "last-name" -> optText.toText.verifying(lastNameNotEntered andThen lastNameInvalid),
        "date-of-birth" -> localDate(
          invalidKey = "error.no_entry_dob",
          invalidDayKey = "error.invalid_dob_day",
          invalidMonthKey = "error.invalid_dob_month",
          allRequiredKey = "error.no_entry_dob",
          twoRequiredKey = "error.no_entry_dob_two_required",
          requiredKey = "error.no_entry_dob_one_required"
        ).verifying(dobYearInvalid andThen invalidAge))(PersonalDetailsModel.apply)(PersonalDetailsModel.unapply)
    )
  }
}
