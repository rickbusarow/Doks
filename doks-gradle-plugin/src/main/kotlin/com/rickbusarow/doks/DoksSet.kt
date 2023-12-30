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

package com.rickbusarow.doks

import com.rickbusarow.doks.internal.gradle.DoksTaskFactory
import com.rickbusarow.doks.internal.gradle.fileTree
import com.rickbusarow.doks.internal.stdlib.applyEach
import org.gradle.api.Action
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.file.Directory
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import java.io.File
import java.nio.file.Path
import javax.inject.Inject

/**
 * An abstract source set for doks documentation and sample code. Provides a [RuleFactory]
 * to allow for the creation of rules to be applied to the source set's documentation.
 *
 * @since 0.1.0
 */
@DoksDsl
@Suppress("MemberVisibilityCanBePrivate")
abstract class DoksSet @Inject constructor(
  /**
   * any arbitrary unique name, like "main" or "tutorials"
   *
   * @since 0.1.0
   */
  val name: String,
  private val objects: ObjectFactory,
  private val layout: ProjectLayout
) : RuleFactory,
  java.io.Serializable {
  /**
   * The documentation files in this source set. This is a
   * [ConfigurableFileCollection], meaning that it can be dynamically configured.
   *
   * If no value is set, a default collection is configured
   * per [DoksTaskFactory.docsFileCollectionDefault].
   *
   * @since 0.1.0
   */
  abstract val docs: ConfigurableFileCollection

  /**
   * The sample code sources for this source set. This is a
   * [ConfigurableFileCollection], meaning that it can be dynamically configured.
   *
   * If no value is set, a default collection is configured
   * per [DoksTaskFactory.samplesFileCollectionDefault].
   *
   * @since 0.1.0
   */
  abstract val sampleCodeSource: ConfigurableFileCollection

  /**
   * Adds a set of document paths to this source set. The given paths
   * are evaluated as per [Project.files][org.gradle.api.Project.files].
   *
   * If any [paths] element represents a directory (a [java.io.File], a
   * [org.gradle.api.file.Directory], a [java.nio.file.Path], or a String path), that element
   * will be converted to a [ConfigurableFileTree] and added to the [docs] collection.
   *
   * Addition to [docs] is done via [ConfigurableFileCollection.from]. For other operations
   * such as [setFrom][ConfigurableFileCollection.setFrom], access the [docs] property directly.
   *
   * @param paths The files to add.
   * @since 0.1.0
   */
  fun docs(vararg paths: Any): ConfigurableFileCollection {
    return docs.applyEach(paths.toList()) { path ->
      from(path.asFileTreeOrAny())
    }
  }

  /**
   * Adds a set of document paths to this source set.
   *
   * Addition to [docs] is done via [ConfigurableFileCollection.from]. For other operations
   * such as [setFrom][ConfigurableFileCollection.setFrom], access the [docs] property directly.
   *
   * @param baseDir The base directory of the file tree. Evaluated
   *   as per [Project.file][org.gradle.api.Project.file].
   * @param configureAction Action to configure the [ConfigurableFileTree] object.
   * @since 0.1.0
   */
  fun docs(
    baseDir: Any,
    configureAction: Action<in ConfigurableFileTree>
  ): ConfigurableFileCollection {
    return docs.from(objects.fileTree(baseDir, configureAction))
  }

  /**
   * Adds a set of sample code paths to this source set. The given paths are
   * *mostly* evaluated as per [Project.files][org.gradle.api.Project.files].
   *
   * If any [paths] element represents a directory (a [java.io.File], a
   * [org.gradle.api.file.Directory], a [java.nio.file.Path], or a String path), that element
   * will be converted to a [ConfigurableFileTree] and added to the [sampleCodeSource] collection.
   *
   * Addition to [sampleCodeSource] is done via [ConfigurableFileCollection.from].
   * For other operations such as [setFrom][ConfigurableFileCollection.setFrom],
   * access the [sampleCodeSource] property directly.
   *
   * @param paths The files to add.
   * @since 0.1.0
   */
  fun sampleCodeSource(vararg paths: Any): ConfigurableFileCollection {
    return sampleCodeSource.applyEach(paths.toList()) { path ->
      from(path.asFileTreeOrAny())
    }
  }

  /**
   * Adds a set of sample code paths to this source set.
   *
   * Addition to [sampleCodeSource] is done via [ConfigurableFileCollection.from].
   * For other operations such as [setFrom][ConfigurableFileCollection.setFrom],
   * access the [sampleCodeSource] property directly.
   *
   * @param baseDir The base directory of the file tree. Evaluated
   *   as per [Project.file][org.gradle.api.Project.file].
   * @param configureAction Action to configure the [ConfigurableFileTree] object.
   * @since 0.1.0
   */
  fun sampleCodeSource(
    baseDir: Any,
    configureAction: Action<in ConfigurableFileTree>
  ): ConfigurableFileCollection {
    return sampleCodeSource.from(objects.fileTree(baseDir, configureAction))
  }

  /**
   * If this `Any` represents a directory, then turn that directory into a
   * file tree. If it's anything else, including an individual File (as in a
   * non-directory), then just return it as itself. This basically means that any
   * directory path arguments are treated as file trees without the extra syntax.
   *
   * @since 0.1.0
   */
  private fun Any.asFileTreeOrAny(): Any {

    val maybeFile =
      when (this) {
        is File -> this
        is Directory -> asFile
        is Path -> toFile()
        is String ->
          layout
            .projectDirectory
            .asFile
            .resolve(this)
            .takeIf { it.exists() }

        else -> null
      }

    return if (maybeFile?.isDirectory == true) {
      sampleCodeSource.from(objects.fileTree(maybeFile))
    } else {
      this
    }
  }
}
