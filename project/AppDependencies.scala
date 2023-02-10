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

import sbt._

object AppDependencies {
  
  private val bootstrapVersion = "7.13.0"

  val compile = Seq(
    "uk.gov.hmrc"           %% "bootstrap-backend-play-28" % bootstrapVersion,
    "com.typesafe.play"     %% "play-json-joda"            % "2.9.4",
    "uk.gov.hmrc"           %% "domain"                    % "8.1.0-play-28"
  )

  val test = Set(
    "uk.gov.hmrc"            %% "bootstrap-test-play-28"      % bootstrapVersion % "test,it",
    "org.scalatestplus"      %% "mockito-4-6"                 % "3.2.15.0"       % "test,it",
    "com.vladsch.flexmark"   %  "flexmark-profile-pegdown"    % "0.64.0"         % "test,it",
    "org.mongodb.scala"      %% "mongo-scala-driver"          % "4.8.1"          % "it"
  )

}
