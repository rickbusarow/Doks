/*
 * Copyright (C) 2023 Rick Busarow
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

import builds.VERSION_NAME
import builds.mustRunAfter
import com.rickbusarow.docusync.gradle.DocusyncTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
  id("root")
  alias(libs.plugins.moduleCheck)
  alias(libs.plugins.github.release)
  alias(libs.plugins.docusync)
}

moduleCheck {
  deleteUnused = true
  checks.sortDependencies = true
}

docusync {
  docSet {
    docs("README.md", "CHANGELOG.md")

    sampleCodeSource("docusync-gradle-plugin/src/integration/kotlin") {
      include("**/*.kt")
    }

    rule("kotlin-dsl-config-simple") {
      replacement = sourceCode(
        fqName = "com.rickbusarow.docusync.gradle.ConfigTest.`kotlin dsl config simple`.config",
        bodyOnly = true,
        codeBlockLanguage = "kotlin",
        attributes = "title=\"build.gradle.kts\""
      )
    }

    rule("kotlin-dsl-config-code") {
      replacement = sourceCode(
        fqName = "com.rickbusarow.docusync.gradle.ConfigTest.`kotlin dsl config code`.config",
        bodyOnly = true,
        codeBlockLanguage = "kotlin",
        attributes = "title=\"build.gradle.kts\""
      )
    }

    rule("dollar-raw-string") {
      regex = Regex.escape("\${'$'}")
      replacement = Regex.escapeReplacement("$")
    }
    rule("buildConfig-version") {
      regex = Regex.escape("\${BuildConfig.version}")
      replacement = Regex.escapeReplacement(VERSION_NAME)
    }
  }
}

subprojects.map {
  it.tasks.withType(KotlinCompile::class.java)
    .mustRunAfter(tasks.withType(DocusyncTask::class.java))
}

// TODO move this to a convention plugin
githubRelease {

  token {
    property("GITHUB_PERSONAL_ACCESS_TOKEN") as? String
      ?: throw GradleException(
        "In order to release, you must provide a GitHub Personal Access Token " +
          "as a property named 'GITHUB_PERSONAL_ACCESS_TOKEN'."
      )
  }

  owner.set("rbusarow")

  tagName { VERSION_NAME }
  releaseName { VERSION_NAME }

  generateReleaseNotes.set(false)

  body {

    if (VERSION_NAME.endsWith("-SNAPSHOT")) {
      throw GradleException(
        "do not create a GitHub release for a snapshot. (version is $VERSION_NAME)."
      )
    }

    val escapedVersion = Regex.escape(VERSION_NAME)

    // capture everything in between '## [<this version>]' and a new line which starts with '## '
    val versionSectionRegex = Regex(
      """(?:^|\n)## \[$escapedVersion]\s+.+\n([\s\S]*?)(?=\n+## |\[$escapedVersion])"""
    )

    versionSectionRegex
      .find(file("CHANGELOG.md").readText())
      ?.groupValues
      ?.getOrNull(1)
      ?.trim()
      ?.also { body ->
        if (body.isBlank()) {
          throw GradleException("The changelog for this version cannot be blank.")
        }
      }
      ?: throw GradleException(
        "could not find a matching change log for $versionSectionRegex"
      )
  }

  overwrite.set(false)
  dryRun.set(false)
  draft.set(true)
}
