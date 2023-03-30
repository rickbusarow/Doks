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
import com.rickbusarow.doks.internal.gradle.registerOnce
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.file.ProjectLayout
import org.gradle.api.tasks.TaskContainer
import javax.inject.Inject

/** @since 0.1.0 */
abstract class DoksExtension @Inject constructor(
  private val taskContainer: TaskContainer,
  private val layout: ProjectLayout
) : java.io.Serializable {

  /** @since 0.1.0 */
  abstract val doksSets: NamedDomainObjectContainer<DoksSet>

  private val taskFactory: DoksTaskFactory by lazy {
    DoksTaskFactory(
      taskContainer = taskContainer,
      layout = layout
    )
  }

  /**
   * Convenience method for defining a new [DoksSet].
   *
   * @param name The name of the new source set. Defaults to "main".
   * @param action The configuration action for the new source set.
   * @return The provider for the new source set.
   * @since 0.1.0
   */
  fun dokSet(
    name: String = "all",
    action: Action<DoksSet>
  ): NamedDomainObjectProvider<DoksSet> {

    return doksSets.registerOnce(name, action)
      .also { sourceSet ->

        taskFactory.registerAll(
          name = name,
          sourceSet = sourceSet
        )
      }
  }
}
