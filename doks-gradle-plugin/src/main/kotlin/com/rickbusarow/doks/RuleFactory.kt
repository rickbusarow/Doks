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

import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectProvider
import org.intellij.lang.annotations.Language

/**
 * An interface for defining and registering [Rule][com.rickbusarow.doks.internal.RuleSerializable] instances.
 *
 * A rule is defined as a search and replace operation, where a given regex
 * pattern is matched against some text and replaced with a specified string.
 *
 * @since 0.1.0
 */
@DoksDsl
public interface RuleFactory : java.io.Serializable {

  /**
   * All registered [RuleBuilderScope] instances within this [DoksSet].
   *
   * @since 0.1.0
   */
  public val rules: NamedDomainObjectContainer<RuleBuilderScope>

  /**
   * Creates and registers a new [Rule][com.rickbusarow.doks.internal.RuleSerializable] instance.
   *
   * @param name the name to register the rule with
   * @param action an [Action] that configures the [RuleBuilderScope] instance used to
   *   build the rule. Use this to set the regex matcher and the replacement string.
   * @return a [NamedDomainObjectProvider] instance for the newly
   *   created [Rule][com.rickbusarow.doks.internal.RuleSerializable] instance
   * @since 0.1.0
   */
  public fun rule(
    name: String,
    action: Action<RuleBuilderScope>
  ): NamedDomainObjectProvider<RuleBuilderScope> {
    return rules.register(name) { action.execute(it) }
  }

  /**
   * Creates and registers a new [Rule][com.rickbusarow.doks.internal.RuleSerializable]
   * instance with the specified regular expression and replacement string.
   *
   * @param name the name to register the rule with
   * @param regex the regular expression pattern to match against
   * @param replacement the string to replace the matched pattern with
   * @return a [NamedDomainObjectProvider] instance for the newly
   *   created [Rule][com.rickbusarow.doks.internal.RuleSerializable] instance
   * @since 0.1.0
   */
  public fun rule(
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
