package uk.gov.hmrc.soletraderidentificationfrontend.views

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.{MustMatchers, WordSpecLike}
import play.api.libs.ws.WSResponse
import uk.gov.hmrc.soletraderidentificationfrontend.assets.MessageLookup.{Base, CaptureSautr => messages}
import uk.gov.hmrc.soletraderidentificationfrontend.utils.ViewSpecHelper.ElementExtensions


trait CaptureSautrViewTests {
  this: WordSpecLike with MustMatchers =>

  def testCaptureSautrView(result: => WSResponse): Unit = {
    lazy val doc: Document = Jsoup.parse(result.body)

    "have the correct title" in {
      doc.title mustBe messages.title
    }

    "have the correct heading" in {
      doc.getH1Elements.text mustBe messages.heading
    }

    "have the correct first line" in {
      doc.getParagraphs.first.text mustBe messages.line_1
    }

    "have a correct details element" in {
      doc.getSpan("details-summary-text").text mustBe messages.line_2
      doc.getParagraphs.get(1).text mustBe messages.details_line_1
      doc.getParagraphs.get(2).text mustBe messages.details_line_2
      doc.getParagraphs.get(3).text mustBe messages.details_line_3
    }

    "have a continue and confirm button" in {
      doc.getSubmitButton.first.text mustBe Base.saveAndContinue
    }

    "have a save and come back later button" in {
      doc.getSubmitButton.get(1).text mustBe Base.saveAndComeBack
    }
  }
}
