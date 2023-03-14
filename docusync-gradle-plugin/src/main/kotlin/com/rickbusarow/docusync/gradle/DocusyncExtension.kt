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

import com.rickbusarow.docusync.Rule
import com.rickbusarow.docusync.gradle.internal.dependsOn
import com.rickbusarow.docusync.gradle.internal.registerOnce
import com.rickbusarow.docusync.internal.capitalize
import org.gradle.api.Action
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.TaskContainer
import org.intellij.lang.annotations.Language
import javax.inject.Inject

/** */
abstract class DocusyncExtension @Inject constructor(
  private val taskContainer: TaskContainer
) : java.io.Serializable {

  /** */
  abstract val sourceSets: NamedDomainObjectContainer<DocusyncSourceSet>

  /** */
  fun docSet(
    name: String = "main",
    action: Action<DocusyncSourceSet>
  ): NamedDomainObjectProvider<DocusyncSourceSet> {

    fun taskNameBase(): String {
      return when (name) {
        "main" -> "docusync"
        else -> "docusync${name.capitalize()}"
      }
    }

    val check =
      taskContainer.registerOnce("${taskNameBase()}Check", DocusyncTask::class.java) { task ->
        task.group = "Docusync"
        task.description = "Searches for any out-of-date documentation and fails if it finds any."
        task.autoCorrect = false

        val sourceSet = sourceSets.getByName(name)

        task.docs.from(sourceSet.docs)
        task.ruleBuilders.addAll(sourceSet.rules)
        task.outputs.files(sourceSet.docs.files)
      }

    val fix =
      taskContainer.registerOnce("${taskNameBase()}Fix", DocusyncTask::class.java) { task ->
        task.group = "Docusync"
        task.description = "Automatically fixes any out-of-date documentation."
        task.autoCorrect = true

        val sourceSet = sourceSets.getByName(name)

        task.docs.from(sourceSet.docs)
        task.ruleBuilders.addAll(sourceSet.rules)
        task.outputs.files(sourceSet.docs.files)
      }

    if (name != "main") {
      taskContainer.named("docusyncCheck").dependsOn(check)
      taskContainer.named("docusyncFix").dependsOn(fix)
    }

    return sourceSets.register(name, action)
  }
}

/** */
abstract class DocusyncSourceSet : Named, java.io.Serializable {

  /** */
  abstract val docs: ConfigurableFileCollection

  /** */
  abstract val rules: NamedDomainObjectContainer<RuleBuilderScope>

  /**
   * Adds a set of document paths to this source set. The given paths are evaluated as per [Project.files].
   *
   * @param paths The files to add.
   * @return this
   */
  fun docs(vararg paths: Any) {
    docs.from(*paths)
  }

  /** */
  fun rule(
    name: String,
    action: Action<RuleBuilderScope>
  ): NamedDomainObjectProvider<RuleBuilderScope> {
    return rules.register(name, action)
  }

  /** */
  fun rule(
    name: String,
    @Language("regexp") regex: String,
    replacement: String
  ): NamedDomainObjectProvider<RuleBuilderScope> {
    return rules.register(name) {
      it.regex = regex
      it.replacement = replacement
    }
  }
}

/** Models a single replacement action very much like the [Regex] version of [String.replace] */
abstract class RuleBuilderScope : Named, java.io.Serializable {

  /** supports normal Regex semantics including capturing groups like `(.*)` */
  abstract var regex: String

  /** any combination of literal text and $-substitutions */
  abstract var replacement: String

  /** @return a [Rule] from the current values of [regex] and [replacement] */
  fun toRule(): Rule = Rule(
    name = name,
    regex = regex,
    replacement = replacement
  )
}
