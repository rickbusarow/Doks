/*
 * Copyright (C) 2024 Rick Busarow
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

@file:UseSerializers(RegexAsStringSerializer::class)

package com.rickbusarow.doks.internal

import dev.drewhamilton.poko.Poko
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.intellij.lang.annotations.Language

/**
 * Models a single replacement action very much like the [Regex] version of [CharSequence.replace]
 *
 * @property name a unique identifier for this rule. It can be any arbitrary string.
 * @property regex supports normal Regex semantics including capturing groups like `(.*)`
 * @property replacement any combination of literal text and $-substitutions
 * @since 0.1.0
 */
@Poko
@Serializable
public class Rule(
  public val name: RuleName,
  public val regex: Regex,
  public val replacement: String
) : java.io.Serializable {

  internal constructor(
    name: String,
    @Language("RegExp")
    regex: String,
    replacement: String
  ) : this(RuleName(name), regex.toRegex(), replacement)

  /**
   * @return a new string obtained by replacing each substring of the [original]
   *   `CharSequence` that matches this [regex] regular expression with the [replacement].
   * @see CharSequence.replace
   * @since 0.1.0
   */
  public fun replaceIn(original: CharSequence): String {
    return original.replace(regex = regex, replacement = replacement)
  }

  @Suppress("RedundantIf")
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is Rule) return false

    if (name != other.name) return false
    if (regex.pattern != other.regex.pattern) return false
    if (replacement != other.replacement) return false

    return true
  }

  override fun hashCode(): Int {
    var result = name.hashCode()
    result = 31 * result + regex.pattern.hashCode()
    result = 31 * result + replacement.hashCode()
    return result
  }
}

/**
 * A unique identifier for a [Rule]. It can be any arbitrary string.
 *
 * @property value the simple String representation of this name
 * @since 0.1.0
 */
@Serializable
@JvmInline
public value class RuleName(public val value: String) :
  java.io.Serializable,
  Comparable<RuleName> {

  override fun compareTo(other: RuleName): Int {
    return value.compareTo(other.value)
  }
}
