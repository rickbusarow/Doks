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

import org.gradle.api.file.ConfigurableFileCollection
import javax.inject.Inject

/**
 * An abstract source set for docusync documentation and sample code. Provides a [RuleFactory] to allow
 * for the creation of rules to be applied to the source set's documentation.
 *
 * @property name any arbitrary unique name, like "main" or "tutorials"
 */
abstract class DocusyncSourceSet @Inject constructor(
  val name: String
) : RuleFactory, java.io.Serializable {

  /**
   * The documentation files in this source set. This is a [ConfigurableFileCollection], meaning that
   * it can be dynamically configured.
   */
  abstract val docs: ConfigurableFileCollection

  /**
   * The sample code sources for this source set. This is a [ConfigurableFileCollection], meaning that
   * it can be dynamically configured.
   */
  abstract val sampleCodeSource: ConfigurableFileCollection

  /**
   * Adds a set of document paths to this source set. The given paths are evaluated as per
   * [Project.files][org.gradle.api.Project.files].
   *
   * @param paths The files to add.
   */
  fun docs(vararg paths: Any) {
    docs.from(*paths)
  }
}
