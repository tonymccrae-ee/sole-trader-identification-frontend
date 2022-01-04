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

package uk.gov.hmrc.soletraderidentificationfrontend.controllers

import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.soletraderidentificationfrontend.config.AppConfig
import uk.gov.hmrc.soletraderidentificationfrontend.services.{JourneyService, SoleTraderIdentificationService}
import uk.gov.hmrc.soletraderidentificationfrontend.views.html.details_not_found_page

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class DetailsNotFoundController @Inject()(mcc: MessagesControllerComponents,
                                          view: details_not_found_page,
                                          val authConnector: AuthConnector,
                                          journeyService: JourneyService,
                                          soleTraderIdentificationService: SoleTraderIdentificationService
                                         )(implicit val config: AppConfig,
                                           executionContext: ExecutionContext) extends FrontendController(mcc) with AuthorisedFunctions {

  def show(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised() {
        journeyService.getJourneyConfig(journeyId).map {
          journeyConfig =>
            Ok(view(
              pageConfig = journeyConfig.pageConfig,
              redirectLocation = routes.DetailsNotFoundController.tryAgain(journeyId),
              journeyId = journeyId
            ))
        }
      }
  }

  def tryAgain(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised() {
        soleTraderIdentificationService.removeAllData(journeyId).map {
          _ => Redirect(routes.CaptureFullNameController.show(journeyId))
        }
      }
  }

}
