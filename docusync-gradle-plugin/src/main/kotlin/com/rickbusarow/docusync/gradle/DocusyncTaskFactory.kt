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

package com.rickbusarow.docusync.gradle

import com.rickbusarow.docusync.gradle.internal.dependsOn
import com.rickbusarow.docusync.gradle.internal.registerOnce
import com.rickbusarow.docusync.internal.stdlib.capitalize
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider

internal class DocusyncTaskFactory(
  private val taskContainer: TaskContainer,
  private val layout: ProjectLayout,
) : java.io.Serializable {

  internal fun registerAll(
    name: String,
    sourceSet: NamedDomainObjectProvider<DocusyncSourceSet>,
  ) {

    val samplesMappingFile = layout.buildDirectory
      .file("tmp/docusync/samples_$name.json")

    val parse = registerParseTask(name, sourceSet, samplesMappingFile)

    val check = registerDocsTask(
      docSetName = name,
      autoCorrect = false,
      sourceSet = sourceSet,
      samplesMappingFile = samplesMappingFile,
      parseTask = parse
    )

    val fix = registerDocsTask(
      docSetName = name,
      autoCorrect = true,
      sourceSet = sourceSet,
      samplesMappingFile = samplesMappingFile,
      parseTask = parse
    )

    if (name != "main") {
      taskContainer.named("docusyncParse").dependsOn(parse)
      taskContainer.named("docusyncCheck").dependsOn(check)
      taskContainer.named("docusync").dependsOn(fix)
    }
  }

  private fun registerParseTask(
    docSetName: String,
    sourceSet: NamedDomainObjectProvider<DocusyncSourceSet>,
    samplesMappingFile: Provider<RegularFile>
  ): TaskProvider<DocusyncParseTask> {
    val taskName = when (docSetName) {
      "main" -> "docusyncParse"
      else -> "docusyncParse${docSetName.capitalize()}"
    }

    return taskContainer.registerOnce(taskName, DocusyncParseTask::class.java) { task ->

      task.sampleCode.from(
        sourceSet.map(DocusyncSourceSet::sampleCodeSource)
      )

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
    sourceSet: NamedDomainObjectProvider<DocusyncSourceSet>,
    samplesMappingFile: Provider<RegularFile>,
    parseTask: TaskProvider<DocusyncParseTask>
  ): TaskProvider<DocusyncDocsTask> {

    val suffix = if (autoCorrect) "" else "Check"

    val taskName = when (docSetName) {
      "main" -> "docusync$suffix"
      else -> "docusync${docSetName.capitalize()}$suffix"
    }

    return taskContainer.registerOnce(taskName, DocusyncDocsTask::class.java) { task ->

      task.autoCorrect = autoCorrect

      task.group = "Docusync"
      task.description = if (autoCorrect) {
        "Automatically fixes any out-of-date documentation."
      } else {
        "Searches for any out-of-date documentation and fails if it finds any."
      }

      task.samplesMapping.set(samplesMappingFile)

      task.onlyIf { sourceSet.map { ss -> ss.docs }.get().files.isNotEmpty() }

      task.docs.from(sourceSet.map { ss -> ss.docs })
      task.ruleBuilders.addAllLater(sourceSet.map { it.rules })

      task.docsShadow.set(layout.buildDirectory.dir("tmp/docusync/$docSetName"))

      task.inputs.files(parseTask)

      task.mustRunAfter(parseTask)
    }
  }
}
