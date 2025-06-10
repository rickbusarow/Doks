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

package com.rickbusarow.doks.internal

import com.rickbusarow.doks.internal.stdlib.requireNotNull
import dev.drewhamilton.poko.Poko

/**
 * Holds all [Rules][RuleSerializable] defined for a given set.
 *
 * @since 0.1.0
 */
@Poko
public class Rules(
  private val map: Map<RuleName, RuleSerializable>
) : java.io.Serializable {

  /**
   * The sorted list of names present in this cache.
   *
   * @since 0.1.0
   */
  public val names: List<RuleName> get() = map.keys.sorted()

  internal constructor(globalRules: List<RuleSerializable>) : this(
    globalRules.associateBy {
      it.name
    }
  )
  internal constructor(
    vararg globalRules: RuleSerializable
  ) : this(globalRules.associateBy { it.name })

  /**
   * @return true if a rule named [ruleName] is defined in this cache
   * @since 0.1.0
   */
  public fun hasName(ruleName: RuleName): Boolean = map.containsKey(ruleName)

  /**
   * @return the [RuleSerializable] associated with [ruleName]
   *   within this scope, or `null` if there's no match
   * @see get for a non-nullable version which throws if the name is missing
   * @since 0.1.0
   */
  public fun getOrNull(ruleName: RuleName): RuleSerializable? {
    return map[ruleName]
  }

  /**
   * @return the [RuleSerializable] associated with [ruleName] within this scope
   * @see getOrNull for a safe version which returns null for a missing name
   * @since 0.1.0
   * @throws IllegalArgumentException if there is no rule with the requested name
   */
  public operator fun get(ruleName: RuleName): RuleSerializable {
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
