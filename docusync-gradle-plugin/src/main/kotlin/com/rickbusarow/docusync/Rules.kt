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

package com.rickbusarow.docusync

import com.rickbusarow.docusync.internal.requireNotNull

/** Holds all [Rules][Rule] defined for a given set. */
class Rules(
  private val map: Map<RuleName, Rule>
) : java.io.Serializable {

  internal constructor(globalRules: List<Rule>) : this(globalRules.associateBy { it.name })
  internal constructor(vararg globalRules: Rule) : this(globalRules.associateBy { it.name })

  /**
   * The sorted list of names present in this cache
   */
  val names: List<RuleName> get() = map.keys.sorted()

  /**
   * @return true if a rule named [ruleName] is defined in this cache
   */
  fun hasName(ruleName: RuleName): Boolean = map.containsKey(ruleName)

  /**
   * @return the [Rule] associated with [ruleName] within this scope, or `null` if there's no match
   * @see get for a non-nullable version which throws if the name is missing
   */
  fun getOrNull(ruleName: RuleName): Rule? {
    return map[ruleName]
  }

  /**
   * @return the [Rule] associated with [ruleName] within this scope
   * @throws IllegalArgumentException if there is no rule with the requested name
   * @see getOrNull for a safe version which returns null for a missing name
   */
  operator fun get(ruleName: RuleName): Rule {
    return map[ruleName]
      .requireNotNull {
        buildString {
          appendLine("There is no defined rule for this name.")
          appendLine("The requested name: `${ruleName.value}`")
          appendLine("The existing names:")
          for (name in map.keys.sorted()) {
            appendLine("\t`${name.value}`")
          }
        }
      }
  }
}
