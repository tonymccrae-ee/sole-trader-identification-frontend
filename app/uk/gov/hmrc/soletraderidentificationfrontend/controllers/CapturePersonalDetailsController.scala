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

package uk.gov.hmrc.soletraderidentificationfrontend.controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.soletraderidentificationfrontend.config.AppConfig
import uk.gov.hmrc.soletraderidentificationfrontend.forms.CapturePersonalDetailsForm
import uk.gov.hmrc.soletraderidentificationfrontend.services.PersonalDetailsStorageService
import uk.gov.hmrc.soletraderidentificationfrontend.views.html.capture_personal_details_page

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CapturePersonalDetailsController @Inject()(mcc: MessagesControllerComponents,
                                                 view: capture_personal_details_page,
                                                 personalDetailsForm: CapturePersonalDetailsForm,
                                                 personalDetailsStorageService: PersonalDetailsStorageService
                                                )(implicit val config: AppConfig, ec: ExecutionContext) extends FrontendController(mcc) {

  def show(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      Future.successful(Ok(view(routes.CapturePersonalDetailsController.submit(journeyId), personalDetailsForm.apply())))
  }

  def submit(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      personalDetailsForm.apply().bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(routes.CapturePersonalDetailsController.submit(journeyId), formWithErrors))),
        personalDetails =>
          personalDetailsStorageService.storePersonalDetails(journeyId, personalDetails).map {
            _ => Redirect(routes.CaptureNinoController.show(journeyId))
          }
      )
  }

}
