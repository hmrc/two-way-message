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

import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc"           %% "bootstrap-backend-play-28" % "7.12.0",
    "com.google.inject"     % "guice"                      % "4.2.0",
    "com.kenshoo"           %% "metrics-play"              % "2.6.6_0.6.2",
    "com.typesafe.play"     %% "play-json-joda"            % "2.9.3",
    "uk.gov.hmrc"           %% "domain"                    % "8.1.0-play-28",
    "javax.inject"          % "javax.inject"               % "1"
  )

  val test = Set(
    "uk.gov.hmrc"            %% "service-integration-test"    % "1.3.0-play-28" % "test,it",
    "org.scalatest"          %% "scalatest"                   % "3.0.5"         % "test",
    "com.typesafe.play"      %% "play-test"                   % current         % "test",
    "org.mockito"            % "mockito-core"                 % "2.23.4"        % "test",
    "org.pegdown"            % "pegdown"                      % "1.6.0"         % "test,it",
    "com.github.tomakehurst" % "wiremock-standalone"          % "2.20.0"        % "test,it",
    "org.scalacheck"         %% "scalacheck"                  % "1.14.0"        % "test,it",
    "org.scalamock"          %% "scalamock-scalatest-support" % "3.6.0"         % "test,it",
    "org.scalatestplus.play" %% "scalatestplus-play"          % "3.1.2"         % "test,it",
    "io.github.cloudify"     %% "spdf"                        % "1.4.0"         % "test"
  )

}
