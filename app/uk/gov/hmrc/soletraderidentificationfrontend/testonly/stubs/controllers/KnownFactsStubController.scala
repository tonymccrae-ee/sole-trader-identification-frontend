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

package uk.gov.hmrc.soletraderidentificationfrontend.testonly.stubs.controllers

import play.api.libs.json.{JsObject, JsSuccess, JsValue, Json}
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class KnownFactsStubController @Inject()(controllerComponents: ControllerComponents) extends BackendController(controllerComponents) {

  def stubKnownFacts: Action[JsValue] = Action.async(parse.json) {
    implicit request =>
      val knownFacts = (request.body \ "knownFacts").head.validate[JsObject]

      knownFacts match {
        case JsSuccess(sautrBlock, _) =>
          (sautrBlock \ "value").validate[String] match {
            case JsSuccess("0000000000", _) =>
              Future.successful(Ok(Json.obj(
                "service" -> "IR-SA",
                "enrolments" -> Json.arr(
                  Json.obj(
                    "identifiers" -> Json.arr(
                      Json.obj(
                        "key" -> "UTR",
                        "value" -> "0000000000"
                      )
                    ),
                    "verifiers" -> Json.arr(
                      Json.obj(
                        "key" -> "IsAbroad",
                        "value" -> "Y"
                      )
                    )
                  )
                )
              )))
            case JsSuccess(sautr, _) =>
              Future.successful(Ok(Json.obj(
                "service" -> "IR-SA",
                "enrolments" -> Json.arr(
                  Json.obj(
                    "identifiers" -> Json.arr(
                      Json.obj(
                        "key" -> "UTR",
                        "value" -> sautr
                      )
                    ),
                    "verifiers" -> Json.arr(
                      Json.obj(
                        "key" -> "PostCode",
                        "value" -> "AA11AA"
                      )
                    )
                  )
                )
              )))
          }
        case _ => throw new InternalServerException("KnownFactsStubController: Error in parsing data posted to stub")
      }
  }

}
