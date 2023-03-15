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
import com.rickbusarow.docusync.Rules
import com.rickbusarow.docusync.psi.DocusyncPsiFileFactory
import com.rickbusarow.docusync.psi.NamedSamples
import com.rickbusarow.docusync.psi.SampleRequest
import com.rickbusarow.docusync.psi.SampleResult
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
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
@Suppress("UnnecessaryAbstractClass")
abstract class DocusyncTask(description: String) : DefaultTask() {
  init {
    group = "Docusync"
    this.description = description
  }

  @delegate:Transient
  @get:Internal
  protected val json: Json by lazy {
    Json {
      prettyPrint = true
      allowStructuredMapKeys = true
    }
  }
}

/** */
abstract class DocusyncParseTask : DocusyncTask("Parses source files for requested samples") {

  /** */
  @get:Input
  internal abstract val sampleRequests: ListProperty<SampleRequest>

  /** */
  @get:InputFiles
  abstract val sampleCode: ConfigurableFileCollection

  /** */
  @get:OutputFile
  abstract val samplesMapping: RegularFileProperty

  /** */
  @TaskAction
  fun execute() {

    val namedSamples = NamedSamples(DocusyncPsiFileFactory())

    val requests = sampleRequests.get()
      .map { SampleRequest(it.fqName, it.bodyOnly) }

    val results = namedSamples.findAll(sampleCode.files.toList(), requests)
      .map { SampleResult(request = it.request, content = it.content) }

    val jsonString = json.encodeToString(results.associateBy { it.request })

    with(samplesMapping.get().asFile) {
      parentFile?.mkdirs()
      writeText(jsonString)
    }
  }
}

/** */
abstract class DocusyncDocsTask @Inject constructor(
  private val workerExecutor: WorkerExecutor,
  objects: ObjectFactory
) : DocusyncTask("Updates documentation files") {

  /** */
  @get:InputFile
  abstract val samplesMapping: RegularFileProperty

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

    val jsonString = samplesMapping.get().asFile.readText()

    val resultsByRequest = json.decodeFromString<Map<SampleRequest, SampleResult>>(jsonString)

    val resultsByRequestHash = resultsByRequest
      .mapKeys { (request, _) -> request.hashCode() }

    val rules = ruleBuilders.toList()
      .map {
        val withSamples = it.replacement.replace("\u200B(-?\\d+)\u200B".toRegex()) { mr ->
          resultsByRequestHash.getValue(mr.groupValues[1].toInt()).content
        }
        Rule(
          name = it.name,
          regex = it.requireRegex(),
          replacement = withSamples
        )
      }
      .associateBy { it.name }

    val engine = DocusyncEngine(
      ruleCache = Rules(rules),
      autoCorrect = autoCorrect
    )

    val changed = inputChanges.getFileChanges(docs)
      .mapNotNull { fileChange -> fileChange.file.takeIf { it.isFile } }

    val queue = workerExecutor.classLoaderIsolation()

    changed.forEach { file ->
      queue.submit(DocsWorkAction::class.java) { params ->
        params.docusyncEngine.set(engine)
        params.file.set(file)
      }
    }
  }

  /** */
  interface DocsParameters : WorkParameters {

    /** */
    val docusyncEngine: Property<DocusyncEngine>

    /** */
    val file: RegularFileProperty
  }

  /** */
  abstract class DocsWorkAction : WorkAction<DocsParameters> {
    override fun execute() {

      val engine = parameters.docusyncEngine.get()

      val file = parameters.file.get().asFile

      engine.run(file)
    }
  }
}
