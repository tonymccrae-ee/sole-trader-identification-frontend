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

import play.api.test.Helpers._
import uk.gov.hmrc.soletraderidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.soletraderidentificationfrontend.views.CapturePersonalDetailsViewTests

class CapturePersonalDetailsControllerISpec extends ComponentSpecHelper with CapturePersonalDetailsViewTests {

  "GET /personal-details" should {
    lazy val result = get("/personal-details")

    "return OK" in {
      result.status mustBe OK
    }

    "return a view which" should {
      testCapturePersonalDetailsView(result)
    }
  }

  "POST /personal-details" should {
    lazy val result = post("/personal-details")()

    "redirect to the Capture Nino page" in {
      result must have(
        httpStatus(SEE_OTHER),
        redirectUri(routes.CaptureNinoController.show().url)
      )
    }
  }
}
