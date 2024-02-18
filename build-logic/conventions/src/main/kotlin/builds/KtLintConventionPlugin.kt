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

import com.rickbusarow.ktlint.KtLintExtension
import com.rickbusarow.ktlint.KtLintPlugin
import com.rickbusarow.ktlint.KtLintTask
import kotlinx.validation.KotlinApiBuildTask
import kotlinx.validation.KotlinApiCompareTask
import org.gradle.api.Plugin
import org.gradle.api.Project

abstract class KtLintConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {

    target.plugins.apply(KtLintPlugin::class.java)

    target.dependencies
      .addProvider("ktlint", target.libs.rickBusarow.ktrules)

    target.tasks.withType(KtLintTask::class.java).configureEach { task ->
      task.mustRunAfter(
        target.tasks.matchingName("apiDump"),
        target.tasks.matchingName("dependencyGuard"),
        target.tasks.matchingName("dependencyGuardBaseline"),
        target.tasks.withType(KotlinApiBuildTask::class.java),
        target.tasks.withType(KotlinApiCompareTask::class.java)
      )

      System.setProperty("ktrules.project_version", target.VERSION_NAME)
    }

    target.extensions.configure(KtLintExtension::class.java) {
      it.ktlintVersion.set(target.libs.versions.ktlint.lib)
    }
  }
}
