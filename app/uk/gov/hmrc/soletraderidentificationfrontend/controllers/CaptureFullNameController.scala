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
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.soletraderidentificationfrontend.forms.CaptureFullNameForm
import uk.gov.hmrc.soletraderidentificationfrontend.services.SoleTraderIdentificationService
import uk.gov.hmrc.soletraderidentificationfrontend.views.html.capture_full_name_page

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CaptureFullNameController @Inject()(mcc: MessagesControllerComponents,
                                          view: capture_full_name_page,
                                          captureFullNameForm: CaptureFullNameForm,
                                          soleTraderIdentificationService: SoleTraderIdentificationService,
                                          val authConnector: AuthConnector
                                         )(implicit ec: ExecutionContext) extends FrontendController(mcc) with AuthorisedFunctions {

  def show(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised() {
        Future.successful(Ok(view(routes.CaptureFullNameController.submit(journeyId), captureFullNameForm.apply())))
      }
  }

  def submit(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised() {
        captureFullNameForm.apply().bindFromRequest().fold(
          formWithErrors =>
            Future.successful(
              BadRequest(view(routes.CaptureFullNameController.submit(journeyId), formWithErrors))
            ),
          fullName =>
            soleTraderIdentificationService.storeFullName(journeyId, fullName).map {
              _ => Redirect(routes.CaptureDateOfBirthController.show(journeyId))
            }
        )
      }
  }

}
