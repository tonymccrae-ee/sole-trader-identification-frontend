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

import javax.inject.{Inject, Singleton}
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.soletraderidentificationfrontend.config.AppConfig
import uk.gov.hmrc.soletraderidentificationfrontend.forms.CaptureNinoForm
import uk.gov.hmrc.soletraderidentificationfrontend.services.NinoStorageService
import uk.gov.hmrc.soletraderidentificationfrontend.views.html.capture_nino_page

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CaptureNinoController @Inject()(mcc: MessagesControllerComponents,
                                      view: capture_nino_page,
                                      ninoStorageService: NinoStorageService)
                                     (implicit val config: AppConfig, executionContext: ExecutionContext) extends FrontendController(mcc) {
  val name = "John Smith" // TODO this will be pre-pop data


  def show(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      Future.successful(Ok(view(routes.CaptureNinoController.submit(journeyId), name, CaptureNinoForm.form)))
  }

  def submit(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      CaptureNinoForm.form.bindFromRequest().fold(
        formWithErrors => Future.successful(
          BadRequest(view(routes.CaptureNinoController.submit(journeyId), name, formWithErrors))
        ),
        nino => ninoStorageService.storeNino(journeyId, nino).map {
          _ => Redirect(routes.CaptureSautrController.show(journeyId))
        }
      )
  }
}
