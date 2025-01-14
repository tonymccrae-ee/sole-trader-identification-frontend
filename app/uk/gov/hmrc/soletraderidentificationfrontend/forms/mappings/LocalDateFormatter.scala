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

package uk.gov.hmrc.soletraderidentificationfrontend.forms.mappings

import play.api.data.FormError
import play.api.data.format.Formatter
import play.api.i18n.Messages
import uk.gov.hmrc.soletraderidentificationfrontend.filters.InputFilter

import java.time.LocalDate
import scala.util.{Failure, Success, Try}

private[mappings] class LocalDateFormatter(
                                            invalidKey: String,
                                            invalidDayKey: String,
                                            invalidMonthKey: String,
                                            allRequiredKey: String,
                                            twoRequiredKey: String,
                                            requiredKey: String,
                                            args: Seq[String] = Seq.empty
                                          )(implicit messages: Messages) extends Formatter[LocalDate] with Formatters with InputFilter {

  private val fieldKeys: List[String] = List("day", "month", "year")

  private def toDate(key: String, day: Int, month: Int, year: Int): Either[Seq[FormError], LocalDate] =
    Try(LocalDate.of(year, month, day)) match {
      case Success(date) =>
        Right(date)
      case Failure(_) if day < 1 || day > 31 => Left(Seq(FormError(key, invalidDayKey, args)))
      case Failure(_) if month < 1 || month > 12 => Left(Seq(FormError(key, invalidMonthKey, args)))
    }

  private def formatDate(key: String, data: Map[String, String]): Either[Seq[FormError], LocalDate] = {

    val int = intFormatter(
      requiredKey = invalidKey,
      wholeNumberKey = invalidKey,
      nonNumericKey = invalidKey,
      args
    )

    for {
      day <- int.bind(s"$key-day", data).right
      month <- int.bind(s"$key-month", data).right
      year <- int.bind(s"$key-year", data).right
      date <- toDate(key, day, month, year).right
    } yield date
  }

  override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], LocalDate] = {

    val fields = fieldKeys.map {
      field =>
        field -> data.get(s"$key-$field").filter(_.nonEmpty).map(f => filter(f))
    }.toMap

    val fieldLabelKeyPrefix = "localDate.label"

    lazy val missingFields = fields
      .withFilter(_._2.isEmpty)
      .map(field => messages(s"$fieldLabelKeyPrefix.${field._1}"))
      .toList

    fields.count(_._2.isDefined) match {
      case 3 =>
        formatDate(key, data).left.map {
          _.map(_.copy(key = key, args = args))
        }
      case 2 =>
        Left(List(FormError(key, requiredKey, missingFields ++ args)))
      case 1 =>
        Left(List(FormError(key, twoRequiredKey, missingFields ++ args)))
      case _ =>
        Left(List(FormError(key, allRequiredKey, args)))
    }
  }

  override def unbind(key: String, value: LocalDate): Map[String, String] =
    Map(
      s"$key-day" -> value.getDayOfMonth.toString,
      s"$key-month" -> value.getMonthValue.toString,
      s"$key-year" -> value.getYear.toString
    )
}
