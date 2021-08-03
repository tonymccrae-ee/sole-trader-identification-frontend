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

import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.soletraderidentificationfrontend.config.AppConfig
import uk.gov.hmrc.soletraderidentificationfrontend.models.{DetailsMismatch, DetailsNotFound, NoSautrProvided, SautrMatched}
import uk.gov.hmrc.soletraderidentificationfrontend.services.{JourneyService, OrchestrationService, SoleTraderIdentificationService}
import uk.gov.hmrc.soletraderidentificationfrontend.views.html.check_your_answers_page

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class CheckYourAnswersController @Inject()(mcc: MessagesControllerComponents,
                                           view: check_your_answers_page,
                                           soleTraderIdentificationService: SoleTraderIdentificationService,
                                           journeyService: JourneyService,
                                           orchestrationService: OrchestrationService,
                                           val authConnector: AuthConnector
                                          )(implicit val config: AppConfig,
                                            executionContext: ExecutionContext) extends FrontendController(mcc) with AuthorisedFunctions {


  def show(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised() {
        soleTraderIdentificationService.retrieveIndividualDetails(journeyId).flatMap {
          case Some(individualDetails) =>
            journeyService.getJourneyConfig(journeyId).map {
              journeyConfig =>
                Ok(view(
                  pageConfig = journeyConfig.pageConfig,
                  formAction = routes.CheckYourAnswersController.submit(journeyId),
                  journeyId = journeyId,
                  authenticatorDetails = individualDetails
                ))
            }
          case _ =>
            throw new InternalServerException("Failed to retrieve data from database")
        }
      }
  }

  def submit(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised() {
        soleTraderIdentificationService.retrieveIndividualDetails(journeyId).flatMap {
          case Some(individualDetails) =>
            orchestrationService.orchestrate(journeyId, individualDetails).map {
              case SautrMatched =>
                Redirect(routes.BusinessVerificationController.startBusinessVerificationJourney(journeyId))
              case NoSautrProvided =>
                Redirect(routes.JourneyRedirectController.redirectToContinueUrl(journeyId))
              case DetailsMismatch =>
                Redirect(routes.PersonalInformationErrorController.show(journeyId))
              case DetailsNotFound =>
                Redirect(routes.DetailsNotFoundController.show(journeyId).url)
            }
          case _ =>
            throw new InternalServerException("Fail to retrieve data from database")
        }
      }
  }
}
