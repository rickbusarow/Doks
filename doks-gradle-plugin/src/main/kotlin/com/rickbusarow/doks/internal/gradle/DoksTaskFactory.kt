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

package com.rickbusarow.doks.internal.gradle

import com.rickbusarow.doks.DoksDocsTask
import com.rickbusarow.doks.DoksParseTask
import com.rickbusarow.doks.DoksSet
import com.rickbusarow.doks.GradleConfiguration
import com.rickbusarow.doks.internal.stdlib.capitalize
import com.rickbusarow.doks.internal.stdlib.createSafely
import com.rickbusarow.doks.internal.stdlib.letIf
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Task
import org.gradle.api.file.FileCollection
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider

internal class DoksTaskFactory(
  private val taskContainer: TaskContainer,
  private val layout: ProjectLayout,
  private val doksDeps: GradleConfiguration,
  private val doksParseDeps: GradleConfiguration
) : java.io.Serializable {

  internal fun registerAll(name: String, sourceSet: NamedDomainObjectProvider<DoksSet>) {

    val nameOrAll = name.ifBlank { "all" }

    val samplesMappingFile = layout.buildDirectory
      .file("tmp/doks/samples_$nameOrAll.json")

    val parse = registerParseTask(
      docSetName = name,
      sourceSet = sourceSet,
      samplesMappingFile = samplesMappingFile,
      doksParseDeps = doksParseDeps
    )

    val check = registerDocsTask(
      docSetName = name,
      autoCorrect = false,
      sourceSet = sourceSet,
      samplesMappingFile = samplesMappingFile,
      parseTask = parse,
      doksDeps = doksDeps
    )

    val fix = registerDocsTask(
      docSetName = name,
      autoCorrect = true,
      sourceSet = sourceSet,
      samplesMappingFile = samplesMappingFile,
      parseTask = parse,
      doksDeps = doksDeps
    )

    check.mustRunAfter(fix)

    if (name.isNotBlank()) {
      taskContainer.named("doksParse").dependsOn(parse)
      taskContainer.named("doksCheck").dependsOn(check)
      taskContainer.named("doks").dependsOn(fix)
    }
  }

  private fun registerParseTask(
    docSetName: String,
    sourceSet: NamedDomainObjectProvider<DoksSet>,
    samplesMappingFile: Provider<RegularFile>,
    doksParseDeps: GradleConfiguration
  ): TaskProvider<DoksParseTask> {

    val taskName = "doksParse${docSetName.capitalize()}"

    return taskContainer.registerOnce(taskName, DoksParseTask::class.java) { task ->

      task.doksParseClasspath.from(doksParseDeps)

      // Get the subproject directories eagerly, outside any provider mappings, so that we're not
      // trying to access the task's project instance during the execution phase. Doing it during the
      // execution phase would break configuration caching.
      val subprojectDirs = task.subprojectDirs()

      val sampleCodeSourceFiles = sourceSet.map { ss ->
        ss.sampleCodeSource
          .letIf(ss.name.isNotBlank() && !ss.sampleCodeSource.hasFiles()) {
            samplesFileCollectionDefault(ss, subprojectDirs)
          }
      }

      task.sampleCode.from(sampleCodeSourceFiles)

      task.onlyIf { task.sampleRequests.orNull?.isEmpty() == false }

      task.sampleRequests.addAll(
        sourceSet.map { docsSet ->
          docsSet.rules.flatMap { it.sampleRequests }
        }
      )

      task.samplesMapping.set(samplesMappingFile)
    }
  }

  private fun registerDocsTask(
    docSetName: String,
    autoCorrect: Boolean,
    sourceSet: NamedDomainObjectProvider<DoksSet>,
    samplesMappingFile: Provider<RegularFile>,
    parseTask: TaskProvider<DoksParseTask>,
    doksDeps: GradleConfiguration
  ): TaskProvider<DoksDocsTask> {

    val taskName = if (autoCorrect) {
      "doks${docSetName.capitalize()}"
    } else {
      "doksCheck${docSetName.capitalize()}"
    }

    return taskContainer.registerOnce(taskName, DoksDocsTask::class.java) { task ->

      task.doksClasspath.from(doksDeps)

      task.autoCorrect = autoCorrect

      task.group = "Doks"
      task.description = if (autoCorrect) {
        "Automatically fixes any out-of-date documentation."
      } else {
        "Searches for any out-of-date documentation and fails if it finds any."
      }

      task.samplesMapping.set(
        samplesMappingFile.map { regularFile ->

          if (!regularFile.asFile.exists()) {
            regularFile.asFile.createSafely()
          }

          regularFile
        }
      )

      // Get the subproject directories eagerly, outside any provider mappings, so that we're not
      // trying to access the task's project instance during the execution phase. Doing it during the
      // execution phase would break configuration caching.
      val subprojectDirs = task.subprojectDirs()

      val docsFiles = sourceSet.map { ss ->
        ss.docs
          .letIf(ss.name.isNotBlank() && !ss.docs.hasFiles()) {
            docsFileCollectionDefault(ss, subprojectDirs)
          }
      }

      task.onlyIf { docsFiles.get().files.isNotEmpty() }

      task.docs.from(docsFiles)
      task.ruleBuilders.addAllLater(sourceSet.map { it.rules })

      task.docsShadow.set(layout.buildDirectory.dir("tmp/doks/$docSetName"))

      task.inputs.files(parseTask)

      task.mustRunAfter(parseTask)
    }
  }

  private fun samplesFileCollectionDefault(ss: DoksSet, subprojectDirs: List<String>) =
    ss.sampleCodeSource(layout.projectDirectory.dir("src")) {
      it.include("**/*.kt", "**/*.kts")
      it.exclude(layout.buildDirectory.get().asFile.path)
      it.exclude(subprojectDirs)
    }

  private fun docsFileCollectionDefault(ss: DoksSet, subprojectDirs: List<String>) =
    ss.docs(layout.projectDirectory.asFile) {
      it.include("**/*.md", "**/*.mdx")
      it.exclude(layout.buildDirectory.get().asFile.path)
      it.exclude(subprojectDirs)
    }

  private fun Task.subprojectDirs() = project.subprojects
    .map { it.projectDir.relativeTo(layout.projectDirectory.asFile).path }

  private fun FileCollection.hasFiles(): Boolean = !filter { it.isFile }.isEmpty
}
