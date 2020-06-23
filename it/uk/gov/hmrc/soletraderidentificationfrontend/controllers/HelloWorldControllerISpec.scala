package uk.gov.hmrc.soletraderidentificationfrontend.controllers

import play.api.test.Helpers.OK
import play.api.libs.ws.WSResponse
import uk.gov.hmrc.soletraderidentificationfrontend.utils.ComponentSpecHelper

class HelloWorldControllerISpec extends ComponentSpecHelper {

  "GET /hello-world" should {
    "return OK" in new Server(defaultApp) {

      val result: WSResponse = get("/hello-world")

      result must have(httpStatus(OK))
    }
  }


}
