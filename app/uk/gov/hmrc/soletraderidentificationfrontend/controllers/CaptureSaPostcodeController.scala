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

import play.api.mvc._
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.soletraderidentificationfrontend.config.AppConfig
import uk.gov.hmrc.soletraderidentificationfrontend.featureswitch.core.config.FeatureSwitching
import uk.gov.hmrc.soletraderidentificationfrontend.forms.CaptureSaPostcodeForm
import uk.gov.hmrc.soletraderidentificationfrontend.services.{JourneyService, SoleTraderIdentificationService}
import uk.gov.hmrc.soletraderidentificationfrontend.views.html.capture_sa_postcode_page

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class CaptureSaPostcodeController @Inject()(mcc: MessagesControllerComponents,
                                            view: capture_sa_postcode_page,
                                            soleTraderIdentificationService: SoleTraderIdentificationService,
                                            val authConnector: AuthConnector,
                                            journeyService: JourneyService
                                           )(implicit val config: AppConfig,
                                             executionContext: ExecutionContext) extends FrontendController(mcc) with AuthorisedFunctions with FeatureSwitching {

  def show(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised() {
        journeyService.getJourneyConfig(journeyId).map {
          journeyConfig =>
            Ok(view(
              journeyId = journeyId,
              pageConfig = journeyConfig.pageConfig,
              formAction = routes.CaptureSaPostcodeController.submit(journeyId),
              form = CaptureSaPostcodeForm.form
            ))
        }
      }
  }

  def submit(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised() {
        CaptureSaPostcodeForm.form.bindFromRequest().fold(
          formWithErrors =>
            journeyService.getJourneyConfig(journeyId).map {
              journeyConfig =>
                BadRequest(view(
                  journeyId = journeyId,
                  pageConfig = journeyConfig.pageConfig,
                  formAction = routes.CaptureSaPostcodeController.submit(journeyId),
                  form = formWithErrors
                ))
            },
          postcode =>
            soleTraderIdentificationService.storeSaPostcode(journeyId, postcode).map {
              _ => Redirect(routes.CaptureOverseasTaxIdentifiersController.show(journeyId))
            }
        )
      }
  }

  def noSaPostcode(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised() {
        soleTraderIdentificationService.removeSaPostcode(journeyId).map {
          _ => Redirect(routes.CaptureOverseasTaxIdentifiersController.show(journeyId))
        }
      }
  }

}
