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
import uk.gov.hmrc.soletraderidentificationfrontend.forms.CaptureOverseasTaxIdentifiersForm
import uk.gov.hmrc.soletraderidentificationfrontend.services.{JourneyService, SoleTraderIdentificationService}
import uk.gov.hmrc.soletraderidentificationfrontend.views.html.capture_overseas_tax_identifiers_page

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class CaptureOverseasTaxIdentifiersController @Inject()(mcc: MessagesControllerComponents,
                                                        journeyService: JourneyService,
                                                        view: capture_overseas_tax_identifiers_page,
                                                        soleTraderIdentificationService: SoleTraderIdentificationService,
                                                        val authConnector: AuthConnector
                                                       )(implicit val config: AppConfig,
                                                         executionContext: ExecutionContext) extends FrontendController(mcc) with AuthorisedFunctions {

  def show(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised() {
        journeyService.getJourneyConfig(journeyId).map {
          journeyConfig =>
            Ok(view(
              journeyId = journeyId,
              pageConfig = journeyConfig.pageConfig,
              formAction = routes.CaptureOverseasTaxIdentifiersController.submit(journeyId),
              form = CaptureOverseasTaxIdentifiersForm.form,
              countries = config.orderedCountryList
            ))
        }
      }
  }

  def submit(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised() {
        CaptureOverseasTaxIdentifiersForm.form.bindFromRequest().fold(
          formWithErrors =>
            journeyService.getJourneyConfig(journeyId).map {
              journeyConfig =>
                BadRequest(view(
                  journeyId = journeyId,
                  pageConfig = journeyConfig.pageConfig,
                  formAction = routes.CaptureOverseasTaxIdentifiersController.submit(journeyId),
                  form = formWithErrors,
                  countries = config.orderedCountryList
                ))
            },
          taxIdentifiers =>
            soleTraderIdentificationService.storeOverseasTaxIdentifiers(journeyId, taxIdentifiers).map {
              _ => Redirect(routes.CheckYourAnswersController.show(journeyId))
            }
        )
      }
  }

  def noOverseasTaxIdentifiers(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised() {
        soleTraderIdentificationService.removeOverseasTaxIdentifiers(journeyId).map {
          _ => Redirect(routes.CheckYourAnswersController.show(journeyId))
        }
      }
  }


}
