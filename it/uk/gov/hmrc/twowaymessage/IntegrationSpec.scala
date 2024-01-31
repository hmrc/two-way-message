/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.twowaymessage

import akka.actor.ActorSystem
import akka.stream.Materializer
import org.mongodb.scala.{ Document, MongoClient, MongoCollection, MongoDatabase }
import org.scalatest.concurrent.{ IntegrationPatience, ScalaFutures }
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.libs.ws.WSClient
import play.api.libs.ws.ahc.{ AhcWSClient, AhcWSClientConfig, StandaloneAhcWSClient }

trait IntegrationSpec
    extends AnyFlatSpec with Matchers with ScalaFutures with IntegrationPatience with GuiceOneServerPerSuite {

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: Materializer = Materializer(system)

  val httpClient: WSClient = new AhcWSClient(StandaloneAhcWSClient(AhcWSClientConfig()))

  val mongoClient: MongoClient = MongoClient()
  val messageDatabase: MongoDatabase = mongoClient.getDatabase("message")
  val messageCollection: MongoCollection[Document] = messageDatabase.getCollection("message")
}
