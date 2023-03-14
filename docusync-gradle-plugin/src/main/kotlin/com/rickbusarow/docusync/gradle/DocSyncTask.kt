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

import com.rickbusarow.docusync.DocusyncEngine
import com.rickbusarow.docusync.Rule
import com.rickbusarow.docusync.RuleCache
import org.gradle.api.DefaultTask
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity.RELATIVE
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.gradle.work.Incremental
import org.gradle.work.InputChanges
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

/** */
abstract class DocusyncTask @Inject constructor(
  private val workerExecutor: WorkerExecutor,
  objects: ObjectFactory
) : DefaultTask() {

  /** */
  @get:Incremental
  @get:InputFiles
  @get:PathSensitive(RELATIVE)
  abstract val docs: ConfigurableFileCollection

  /** */
  @get:Input
  abstract val ruleBuilders: NamedDomainObjectContainer<RuleBuilderScope>

  private val autoCorrectProperty: Property<Boolean> = objects.property(Boolean::class.java)
    .convention(false)

  /** */
  @set:Option(
    option = "autoCorrect",
    description = "If true, Docusync will automatically fix any out-of-date documentation."
  )
  var autoCorrect: Boolean
    @Input
    get() = autoCorrectProperty.get()
    set(value) = autoCorrectProperty.set(value)

  /** */
  @TaskAction
  fun execute(inputChanges: InputChanges) {

    val rules = ruleBuilders.map { it.toRule() }

    val engine = DocusyncEngine(RuleCache(rules), autoCorrect = autoCorrect)

    val changed = inputChanges.getFileChanges(docs)
      .mapNotNull { fileChange -> fileChange.file.takeIf { it.isFile } }

    val queue = workerExecutor.noIsolation()

    changed.forEach { file ->
      queue.submit(DocusyncWorkAction::class.java) { params ->
        params.docusyncEngine.set(engine)
        params.file.set(file)
      }
    }
  }
}

/** */
interface DocusyncParams : WorkParameters {

  /** */
  val docusyncEngine: Property<DocusyncEngine>

  /** */
  val file: RegularFileProperty
}

/** */
abstract class DocusyncWorkAction : WorkAction<DocusyncParams> {
  override fun execute() {

    val engine = parameters.docusyncEngine.get()

    val file = parameters.file.get().asFile

    engine.run(file)
  }
}
