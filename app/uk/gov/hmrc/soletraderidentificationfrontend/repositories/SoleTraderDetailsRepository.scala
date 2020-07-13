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

package uk.gov.hmrc.soletraderidentificationfrontend.repositories

import javax.inject.Inject
import play.api.libs.json._
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.play.json.JSONSerializationPack.Reader
import reactivemongo.play.json._
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.soletraderidentificationfrontend.models.SoleTraderDetailsModel
import uk.gov.hmrc.soletraderidentificationfrontend.repositories.SoleTraderDetailsRepository._

import scala.concurrent.{ExecutionContext, Future}

class SoleTraderDetailsRepository @Inject()(reactiveMongoComponent: ReactiveMongoComponent)(implicit ec: ExecutionContext)
  extends ReactiveRepository[SoleTraderDetailsModel, String](
    collectionName = "sole-trader-details",
    mongo = reactiveMongoComponent.mongoConnector.db,
    domainFormat = SoleTraderDetailsModel.format,
    idFormat = implicitly[Format[String]]
  ) {

  def retrieveNino(journeyId: String): Future[Option[String]] = retrieve[String](journeyId, ninoKey)

  def retrieveSautr(journeyId: String): Future[Option[String]] = retrieve[String](journeyId, sautrKey)


  def storeNino(journeyId: String, nino: String): Future[Unit] =
    findAndUpdate(
      query = Json.obj(
        idKey -> journeyId
      ),
      update = Json.obj("$set" -> Json.obj(ninoKey -> nino)),
      upsert = true
    ).map(_ => ())

  def storeSautr(journeyId: String, sautr: String): Future[Unit] =
    findAndUpdate(
      query = Json.obj(
        idKey -> journeyId
      ),
      update = Json.obj("$set" -> Json.obj(sautrKey -> sautr)),
      upsert = true
    ).map(_ => ())

  private def retrieve[T](journeyId: String, key: String)(implicit reads: Reader[T]): Future[Option[T]] =
    collection.find(
      selector = Json.obj(
        idKey -> journeyId
      ),
      projection = Some(
        Json.obj(
          idKey -> 0,
          key -> 1
        )
      )
    ).one[JsObject] map { doc =>
      doc.flatMap { js =>
        (js \ key).validateOpt[T].get
      }
    }
}

object SoleTraderDetailsRepository {
  val ninoKey = "nino"
  val sautrKey = "sautr"
  val idKey = "_id"
}
