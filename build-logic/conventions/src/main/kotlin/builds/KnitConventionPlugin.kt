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

package builds

import kotlinx.knit.KnitPluginExtension
import kotlinx.knit.KnitTask
import org.gradle.api.Plugin
import org.gradle.api.Project

abstract class KnitConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {

    target.plugins.applyOnce("kotlinx-knit")

    target.extensions.configure(KnitPluginExtension::class.java) { extension ->
      extension.moduleRoots = listOf(".")

      extension.moduleDocs = "build/dokka/htmlMultiModule"
      extension.dokkaMultiModuleRoot = "build/dokka/htmlMultiModule"
      extension.moduleMarkers = listOf("build.gradle", "build.gradle.kts")
      extension.siteRoot = "https://rbusarow.github.io/doks/api"
    }

    target.tasks.withType(KnitTask::class.java) { task ->
      task.notCompatibleWithConfigurationCache("knit does not support configuration cache")

      task.dependsOn("dokkaHtmlMultiModule")

      task.rootDir = target.rootProject.rootDir

      task.files = target.fileTree(target.rootDir) { tree ->
        tree.include(
          "**/*.md",
          "**/*.kt",
          "**/*.kts"
        )
        tree.exclude(
          "**/node_modules/**",
          "**/build/**",
          "**/docs/**",
          "**/versioned_docs/**",
          "**/sample/**",
          "**/.gradle/**"
        )
      }
    }
  }
}
