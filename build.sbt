// Copyright (C) 2011-2012 the original author or authors.
// See the LICENCE.txt file distributed with this work for additional
// information regarding copyright ownership.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

import com.lucidchart.sbt.scalafmt.ScalafmtCorePlugin.autoImport._
import uk.gov.hmrc.DefaultBuildSettings.integrationTestSettings
import uk.gov.hmrc.ExternalService
import uk.gov.hmrc.ServiceManagerPlugin.Keys.itDependenciesList
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings

val appName = "two-way-message"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(publishingSettings: _*)
  .configs(IntegrationTest)
  .settings(integrationTestSettings(): _*)
  .settings(
    resolvers ++= Seq(
      "third-party-maven-releases" at "https://artefacts.tax.service.gov.uk/artifactory/third-party-maven-releases/"
    ),
    inConfig(IntegrationTest)(
      scalafmtCoreSettings ++
        Seq(
          compileInputs in compile := Def.taskDyn {
            val task = test in (resolvedScoped.value.scope in scalafmt.key)
            val previousInputs = (compileInputs in compile).value
            task.map(_ => previousInputs)
          }.value
        ))
  )
  .settings(
    majorVersion := 0,
    scalaVersion := "2.12.12",
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test
  )
  .settings(ServiceManagerPlugin.serviceManagerSettings)
  .settings(
    itDependenciesList := List(
      ExternalService("AUTH"),
      ExternalService("IDENTITY_VERIFICATION"),
      ExternalService("MESSAGE"),
      ExternalService("USER_DETAILS")
    ))
  .settings(ScoverageSettings())
