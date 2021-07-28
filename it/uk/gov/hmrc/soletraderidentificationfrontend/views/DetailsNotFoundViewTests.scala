package uk.gov.hmrc.soletraderidentificationfrontend.views

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.libs.ws.WSResponse
import uk.gov.hmrc.soletraderidentificationfrontend.assets.MessageLookup.{Base, BetaBanner, DetailsNotFound => messages}
import uk.gov.hmrc.soletraderidentificationfrontend.assets.TestConstants.testSignOutUrl
import uk.gov.hmrc.soletraderidentificationfrontend.config.AppConfig
import uk.gov.hmrc.soletraderidentificationfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.soletraderidentificationfrontend.utils.ViewSpecHelper.ElementExtensions

trait DetailsNotFoundViewTests {
  this: ComponentSpecHelper =>

  def testDetailsNotFoundView(result: => WSResponse): Unit = {
    lazy val doc: Document = Jsoup.parse(result.body)
    lazy val config = app.injector.instanceOf[AppConfig]

    "have the correct title" in {
      doc.title mustBe messages.title
    }

    "have sign out link redirecting to signOutUrl from journey config" in {
      doc.getSignOutLink mustBe testSignOutUrl
    }

    "have the correct beta banner" in {
      doc.getBanner.text mustBe BetaBanner.title
    }

    "have a banner link that redirects to beta feedback" in {
      doc.getBannerLink mustBe config.betaFeedbackUrl("vrs")
    }

    "have the correct heading" in {
      doc.getH1Elements.get(0).text mustBe messages.heading
    }

    "have the correct first line" in {
      doc.getParagraphs.get(1).text mustBe messages.line_1
    }

    "have the correct link for contacting Nino Team" in {
      doc.getLink(id = "ninoTeam").text mustBe messages.link_1
    }

    "have the correct second line" in {
      doc.getParagraphs.get(2).text mustBe messages.line_2
    }

    "have the correct link to try again" in {
      doc.getLink(id = "TryAgain").text mustBe messages.link_2
    }


  }
}
