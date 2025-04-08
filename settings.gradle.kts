/*
 * Copyright (C) 2025 Rick Busarow
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

pluginManagement {
  repositories {
    gradlePluginPortal()
    mavenCentral()
    google()
  }
  includeBuild("build-logic")
}

plugins {
  id("com.gradle.develocity") version "4.0"
}

develocity {
  buildScan {

    uploadInBackground = true

    termsOfUseUrl = "https://gradle.com/help/legal-terms-of-use"
    termsOfUseAgree = "yes"

    capture {
      testLogging = true
      buildLogging = true
      fileFingerprints = true
    }

    val inGHA = !System.getenv("GITHUB_ACTIONS").isNullOrEmpty()

    tag(if (inGHA) "CI" else "Local")

    obfuscation {
      hostname { "<host name>" }
      username { "<username>" }
      ipAddresses { List(it.size) { i -> "<ip address $i>" } }
      externalProcessName { "<external process name>" }
    }

    publishing {
      onlyIf { inGHA }
    }

    if (inGHA) {
      // ex: `octocat/Hello-World` as in github.com/octocat/Hello-World
      val repository = System.getenv("GITHUB_REPOSITORY")!!
      val runId = System.getenv("GITHUB_RUN_ID")!!

      link(
        "GitHub Action Run",
        "https://github.com/$repository/actions/runs/$runId"
      )
    }
  }
}

dependencyResolutionManagement {
  @Suppress("UnstableApiUsage")
  repositories {
    google()
    mavenCentral()
    maven("https://plugins.gradle.org/m2/")
  }
}

rootProject.name = "Doks"

include(
  ":doks-gradle-plugin"
)
