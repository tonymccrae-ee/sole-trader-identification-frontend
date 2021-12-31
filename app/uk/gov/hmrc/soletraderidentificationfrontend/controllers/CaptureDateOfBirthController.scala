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

package uk.gov.hmrc.soletraderidentificationfrontend.controllers

import play.api.i18n.Messages
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.soletraderidentificationfrontend.config.AppConfig
import uk.gov.hmrc.soletraderidentificationfrontend.forms.CaptureDateOfBirthForm.captureDateOfBirthForm
import uk.gov.hmrc.soletraderidentificationfrontend.forms.utils.TimeMachine
import uk.gov.hmrc.soletraderidentificationfrontend.models.PageConfig
import uk.gov.hmrc.soletraderidentificationfrontend.services.{JourneyService, SoleTraderIdentificationService}
import uk.gov.hmrc.soletraderidentificationfrontend.views.helpers.CustomPageElementsBuilder
import uk.gov.hmrc.soletraderidentificationfrontend.views.html.capture_date_of_birth_page

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CaptureDateOfBirthController @Inject()(mcc: MessagesControllerComponents,
                                             view: capture_date_of_birth_page,
                                             soleTraderIdentificationService: SoleTraderIdentificationService,
                                             customPageElementsBuilder: CustomPageElementsBuilder,
                                             val authConnector: AuthConnector,
                                             timeMachine: TimeMachine,
                                             journeyService: JourneyService
                                            )(implicit val config: AppConfig,
                                              ec: ExecutionContext) extends FrontendController(mcc) with AuthorisedFunctions {

  def show(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised() {
        retrieveViewData(journeyId).map(viewData => {
          val (pageTitle, pageHeading, pageConfig) = viewData
          Ok(view(
            pageTitle,
            pageHeading,
            pageConfig = pageConfig,
            formAction = routes.CaptureDateOfBirthController.submit(journeyId),
            form = captureDateOfBirthForm(timeMachine.now())
          ))
        })
      }
  }

  def submit(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised() {
        captureDateOfBirthForm(timeMachine.now()).bindFromRequest.fold(
          formWithErrors =>
            retrieveViewData(journeyId).map(viewData => {
              val (pageTitle, pageHeading, pageConfig) = viewData
              BadRequest(view(
                pageTitle,
                pageHeading,
                pageConfig = pageConfig,
                formAction = routes.CaptureDateOfBirthController.submit(journeyId),
                form = formWithErrors)
              )
            }),
          dateOfBirth =>
            soleTraderIdentificationService.storeDateOfBirth(journeyId, dateOfBirth).map {
              _ => Redirect(routes.CaptureNinoController.show(journeyId))
            }
        )
      }
  }

  private def retrieveViewData(journeyId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext, messages: Messages): Future[(String, String, PageConfig)] =
    for {
      journeyConfig <- journeyService.getJourneyConfig(journeyId)
      (pageTitle, pageHeading) <- customPageElementsBuilder.build(
        journeyConfig,
        messagesKeyPrefix = "date-of-birth",
        eventualMaybeAFullName = soleTraderIdentificationService.retrieveFullName(journeyId)
      )
    } yield (pageTitle, pageHeading, journeyConfig.pageConfig)

}
