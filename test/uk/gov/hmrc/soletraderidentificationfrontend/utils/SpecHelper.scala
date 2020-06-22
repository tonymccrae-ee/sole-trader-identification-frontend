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

package uk.gov.hmrc.soletraderidentificationfrontend.utils

import org.scalatestplus.play.PlaySpec
import play.api.{Configuration, Environment, Mode}
import uk.gov.hmrc.play.bootstrap.config.{RunMode, ServicesConfig}
import uk.gov.hmrc.soletraderidentificationfrontend.config.AppConfig

trait SpecHelper extends PlaySpec {
  private val env: Environment = Environment.simple()
  private val configuration: Configuration = Configuration.load(env)
  private val serviceConfig: ServicesConfig = new ServicesConfig(configuration, new RunMode(configuration, Mode.Dev))

  implicit val appConfig: AppConfig = new AppConfig(serviceConfig)
}