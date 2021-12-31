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

package uk.gov.hmrc.soletraderidentificationfrontend.views.helpers

import play.api.i18n.Messages
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.soletraderidentificationfrontend.models.{FullName, JourneyConfig}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CustomPageElementsBuilder @Inject()(implicit val executionContext: ExecutionContext) {

  type MessageKeyBuilder = String => String

  def build(journeyConfig: JourneyConfig,
            messagesKeyPrefix: String,
            eventualMaybeAFullName: => Future[Option[FullName]])(implicit hc: HeaderCarrier, messages: Messages): Future[(String, String)] =
    for {
      theMessageFullKeyBuilder <- messageFullKeyBuilder(messagesKeyPrefix)
      optUseUserFirstName <- checkIfToBuildElementsUsingUserFirstName(journeyConfig, eventualMaybeAFullName)
      pageTitle <- pageTitleBuilder(optUseUserFirstName, theMessageFullKeyBuilder, messages)
      pageHeading <- pageHeadingBuilder(optUseUserFirstName, theMessageFullKeyBuilder, messages)
    } yield (pageTitle, pageHeading)

  private def messageFullKeyBuilder(messagesKeyPrefix: String): Future[MessageKeyBuilder] = Future.successful((keySuffix: String) => s"$messagesKeyPrefix.$keySuffix")

  private def checkIfToBuildElementsUsingUserFirstName(journeyConfig: JourneyConfig,
                                                       fullName: => Future[Option[FullName]])(implicit hc: HeaderCarrier): Future[Option[String]] =
    if (journeyConfig.theUserIsTheApplicant())
      Future.successful(None)
    else
      fullName.flatMap {
        case Some(fullName) => Future.successful(Some(fullName.firstName))
        case None => Future.failed(new IllegalStateException("Full name not found"))
      }

  private def pageTitleBuilder(optUseUserFirstName: Option[String],
                               messageKeyBuilder: MessageKeyBuilder,
                               messages: Messages): Future[String] = Future.successful(optUseUserFirstName match {
    case Some(userFirstName) => messages(messageKeyBuilder("title_with_user_name"), userFirstName)
    case None => messages(messageKeyBuilder("title"))
  })

  private def pageHeadingBuilder(optUseUserFirstName: Option[String],
                                 messageKeyBuilder: MessageKeyBuilder,
                                 messages: Messages): Future[String] = Future.successful(optUseUserFirstName match {
    case Some(userFirstName) => messages(messageKeyBuilder("heading_with_user_name"), userFirstName)
    case None => messages(messageKeyBuilder("heading"))
  })

}
