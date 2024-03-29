/*
 * Copyright (C) 2024 Rick Busarow
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

import java.util.Properties

pluginManagement {
  repositories {
    google()
    mavenCentral()
    maven("https://plugins.gradle.org/m2/")
  }
}

dependencyResolutionManagement {
  @Suppress("UnstableApiUsage")
  repositories {
    google()
    mavenCentral()
    maven("https://plugins.gradle.org/m2/")
  }

  versionCatalogs {
    create("libs") {
      from(files("../gradle/libs.versions.toml"))
    }
  }
}

rootProject.name = "build-logic"

include(
  ":conventions",
  ":core",
  ":module"
)

gradle.beforeProject {

  if (project != rootProject) return@beforeProject

  val extras = project.extra

  file("../gradle.properties").inputStream()
    .use { Properties().apply { load(it) } }
    .asSequence()
    .mapNotNull { (key, value) ->
      when (val k = key) {
        !is String -> null
        extra.has(k) -> null
        else -> (key as String) to value
      }
    }
    .forEach { (key, value) ->
      extras.set(key, value)
    }
}
