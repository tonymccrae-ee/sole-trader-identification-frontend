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

package uk.gov.hmrc.soletraderidentificationfrontend.forms

import java.time.LocalDate

import play.api.data.Forms.{of, single}
import play.api.data.format.Formatter
import play.api.data.validation.Constraint
import play.api.data.{Form, FormError}
import uk.gov.hmrc.soletraderidentificationfrontend.forms.utils.ConstraintUtil.constraint
import uk.gov.hmrc.soletraderidentificationfrontend.forms.utils.ValidationHelper.validateNot

import scala.util.Try

object CaptureDateOfBirthForm {

  val dateKey = "date-of-birth"
  val dayKey = "date-of-birth-day"
  val monthKey = "date-of-birth-month"
  val yearKey = "date-of-birth-year"
  val invalidDateErrorKey = "error.invalid_date"
  val missingDateErrorKey = "error.no_entry_dob"
  val futureDateErrorKey = "error.invalid_dob_future"

  private def invalidAge(currentDate: LocalDate): Constraint[LocalDate] = constraint[LocalDate] {
    val minAge = 16

    dateOfBirth =>
      validateNot(
        constraint = dateOfBirth.isBefore(currentDate.minusYears(minAge)),
        errMsg = "error.invalid_age"
      )
  }

  private val invalidDate: Formatter[LocalDate] = new Formatter[LocalDate] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], LocalDate] = {

      val dayExists = data.getOrElse(dayKey, "").nonEmpty
      val monthExists = data.getOrElse(monthKey, "").nonEmpty
      val yearExists = data.getOrElse(yearKey, "").nonEmpty

      val dateExists = dayExists && monthExists && yearExists

      if (dateExists) {
        val inputDate = Try(
          for {
            day <- data.get(dayKey).map(Integer.parseInt)
            month <- data.get(monthKey).map(Integer.parseInt)
            year <- data.get(yearKey).map(Integer.parseInt).filter(_ > 1900)
          } yield LocalDate.of(year, month, day)
        ).getOrElse(None)

        inputDate match {
          case None => Left(Seq(FormError(dateKey, invalidDateErrorKey)))
          case Some(date) if date.isAfter(LocalDate.now) => Left(Seq(FormError(dateKey, futureDateErrorKey)))
          case Some(date) => Right(date)
        }
      } else {
        Left(Seq(FormError(dateKey, missingDateErrorKey)))
      }
    }

    override def unbind(key: String, value: LocalDate): Map[String, String] = Map(
      "day" -> value.getDayOfMonth.toString,
      "month" -> value.getMonth.toString,
      "year" -> value.getYear.toString
    )
  }


  def captureDateOfBirthForm(currentDate: LocalDate): Form[LocalDate] = Form(
    single(dateKey -> of[LocalDate](invalidDate).verifying(invalidAge(currentDate)))
  )

}


