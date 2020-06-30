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

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import uk.gov.hmrc.soletraderidentificationfrontend.utils.SpecHelper
import uk.gov.hmrc.soletraderidentificationfrontend.views.html.sa_utr_page

class SaUtrControllerSpec extends SpecHelper with GuiceOneAppPerSuite {

  private val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/sa-utr")

  private val testView = app.injector.instanceOf[sa_utr_page]

  object TestController extends SaUtrController(stubMessagesControllerComponents(), testView)

  "GET /sa-utr" should {
    val result = TestController.show(fakeRequest)

    "return 200" in {
      status(result) mustBe Status.OK
    }

    "return HTML" in {
      contentType(result) mustBe Some("text/html")
      charset(result) mustBe Some("utf-8")
    }

  }
}
