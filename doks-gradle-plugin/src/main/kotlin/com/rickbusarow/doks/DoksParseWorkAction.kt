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

package com.rickbusarow.doks

import com.rickbusarow.doks.internal.psi.DoksPsiFileFactory
import com.rickbusarow.doks.internal.psi.NamedSamples
import com.rickbusarow.doks.internal.psi.SampleRequest
import com.rickbusarow.doks.internal.psi.SampleResult
import com.rickbusarow.doks.internal.stdlib.isExistanttKotlinFile
import kotlinx.serialization.json.Json
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

internal typealias GradleProject = org.gradle.api.Project
internal typealias GradleConfiguration = org.gradle.api.artifacts.Configuration
internal typealias GradleLogger = org.gradle.api.logging.Logger
internal typealias GradleLogging = org.gradle.api.logging.Logging
internal typealias GradleProperty<T> = org.gradle.api.provider.Property<T>
internal typealias GradleProvider<T> = org.gradle.api.provider.Provider<T>

/** @since 0.3.0 */
public abstract class DoksParseWorkAction : WorkAction<DoksParseWorkAction.DoksParseWorkParameters> {
  override fun execute() {

    val namedSamples = NamedSamples(DoksPsiFileFactory())

    val requests = parameters.sampleRequests.get()
      .map { SampleRequest(it.fqName, it.bodyOnly) }

    val kotlinFiles = parameters.sampleCode
      .filter { it.isExistanttKotlinFile() }
      .files

    val results = namedSamples.findAll(
      files = kotlinFiles,
      requests = requests
    )
      .map { SampleResult(request = it.request, content = it.content) }

    val json = Json {
      prettyPrint = true
      allowStructuredMapKeys = true
    }

    val jsonString = json.encodeToString(results.associateBy { it.request })

    with(parameters.samplesMapping.get().asFile) {
      parentFile?.mkdirs()
      writeText(jsonString)
    }
  }

  internal interface DoksParseWorkParameters : WorkParameters {
    val sampleRequests: ListProperty<SampleRequest>
    val sampleCode: ConfigurableFileCollection
    val samplesMapping: RegularFileProperty
  }
}
