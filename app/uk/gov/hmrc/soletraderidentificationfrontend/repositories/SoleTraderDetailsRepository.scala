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

package uk.gov.hmrc.soletraderidentificationfrontend.repositories

import javax.inject.Inject
import play.api.libs.json._
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.commands.UpdateWriteResult
import reactivemongo.play.json.JSONSerializationPack.Reader
import reactivemongo.play.json.JsObjectDocumentWriter
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.soletraderidentificationfrontend.models.{PersonalDetailsModel, SoleTraderDetailsModel}
import uk.gov.hmrc.soletraderidentificationfrontend.repositories.SoleTraderDetailsRepository._

import scala.concurrent.{ExecutionContext, Future}

class SoleTraderDetailsRepository @Inject()(reactiveMongoComponent: ReactiveMongoComponent)(implicit ec: ExecutionContext)
  extends ReactiveRepository[SoleTraderDetailsModel, String](
    collectionName = "sole-trader-details",
    mongo = reactiveMongoComponent.mongoConnector.db,
    domainFormat = SoleTraderDetailsModel.format,
    idFormat = implicitly[Format[String]]
  ) {

  def storePersonalDetails(journeyId: String, personalDetails: PersonalDetailsModel): Future[Unit] =
    upsert[PersonalDetailsModel](journeyId, personalDetailsKey, personalDetails).map(_ => ())

  def storeNino(journeyId: String, nino: String): Future[Unit] =
    upsert[String](journeyId, ninoKey, nino).map(_ => ())

  def storeSautr(journeyId: String, sautr: String): Future[Unit] =
    upsert[String](journeyId, sautrKey, sautr).map(_ => ())

  def retrievePersonalDetails(journeyId: String): Future[Option[PersonalDetailsModel]] =
    retrieve[PersonalDetailsModel](journeyId, personalDetailsKey)

  def retrieveNino(journeyId: String): Future[Option[String]] =
    retrieve[String](journeyId, ninoKey)

  def retrieveSautr(journeyId: String): Future[Option[String]] =
    retrieve[String](journeyId, sautrKey)

  private def upsert[T](journeyId: String, key: String, updates: T)
                       (implicit writes: Writes[T]): Future[UpdateWriteResult] =
    collection.update(ordered = false).one(
      q = Json.obj(idKey -> journeyId),
      u = Json.obj(fields = "$set" -> Json.obj(key -> updates)),
      upsert = true
    ).filter(_.n == 1)

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
    ).one[JsObject].map(
      _.map(
        js => (js \ key).as[T]
      )
    )
}

object SoleTraderDetailsRepository {
  val idKey = "_id"
  val personalDetailsKey = "personalDetails"
  val ninoKey = "nino"
  val sautrKey = "sautr"
}