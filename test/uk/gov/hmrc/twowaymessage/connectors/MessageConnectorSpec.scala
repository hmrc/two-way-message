/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.twowaymessage.connectors

import com.codahale.metrics.SharedMetricRegistries
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import org.scalatest.{ BeforeAndAfterAll, BeforeAndAfterEach, Suite }
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Mode
import play.api.http.Status
import play.api.inject.{ Injector, bind }
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{ JsSuccess, Json }
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.twowaymessage.assets.Fixtures
import uk.gov.hmrc.twowaymessage.model.MessageFormat._
import uk.gov.hmrc.twowaymessage.model._

class MessageConnectorSpec
    extends AnyWordSpec with WithWireMock with Matchers with GuiceOneAppPerSuite with Fixtures with MockitoSugar {

  val injector: Injector = new GuiceApplicationBuilder()
    .overrides(bind[Mode].to(Mode.Test))
    .injector()

  val messageConnector: MessageConnector = injector.instanceOf[MessageConnector]

  "GET list of messages via message connector" should {

    "returns 200 successfully for a valid messageId" in {
      val jsonResponseBody = conversationItems("123456", "654321")

      val messageId = "5d12eb115f0000000205c150"
      givenThat(
        get(urlEqualTo(s"/messages-list/$messageId"))
          .willReturn(
            aResponse()
              .withStatus(Status.OK)
              .withBody(jsonResponseBody)))

      val httpResult = await(messageConnector.getMessages(messageId)(new HeaderCarrier()))
      httpResult.status shouldBe 200
      Json.parse(httpResult.body).validate[List[ConversationItem]] shouldBe a[JsSuccess[_]]
    }

    SharedMetricRegistries.clear()
  }
}

trait WithWireMock extends BeforeAndAfterAll with BeforeAndAfterEach {
  suite: Suite =>

  def dependenciesPort = 8910

  lazy val wireMockServer = new WireMockServer(wireMockConfig().port(dependenciesPort))

  override def beforeAll(): Unit = {
    super.beforeAll()
    wireMockServer.start()
    WireMock.configureFor(dependenciesPort)
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    wireMockServer.resetMappings()
    wireMockServer.resetRequests()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    wireMockServer.stop()
  }

}
