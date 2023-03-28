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

package com.rickbusarow.docusync.internal.stdlib

import java.util.Locale

/**
 * Replaces the deprecated Kotlin version, but hard-codes `Locale.US`
 *
 * @since 0.1.0
 */
internal fun String.capitalize(): String = replaceFirstChar {
  if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString()
}

/**
 * shorthand for `joinToString("") { ... }`
 *
 * @since 0.1.0
 */
internal fun <E> Sequence<E>.joinToStringConcat(
  transform: ((E) -> CharSequence)? = null
): String = joinToString("", transform = transform)

/**
 * shorthand for `joinToString("") { ... }`
 *
 * @since 0.1.0
 */
internal fun <E> Iterable<E>.joinToStringConcat(
  transform: ((E) -> CharSequence)? = null
): String = joinToString("", transform = transform)

/**
 * Converts all line separators in the receiver string to use `\n`.
 *
 * @since 0.1.0
 */
internal fun String.normaliseLineSeparators(): String = replace("\r\n|\r".toRegex(), "\n")

/**
 * Adds line breaks and indents to the output of data class `toString()`s.
 *
 * @see toStringPretty
 * @since 0.1.0
 */
internal fun String.prettyToString(): String {
  return replace(",", ",\n")
    .replace("(", "(\n")
    .replace(")", "\n)")
    .replace("[", "[\n")
    .replace("]", "\n]")
    .replace("{", "{\n")
    .replace("}", "\n}")
    .replace("\\(\\s*\\)".toRegex(), "()")
    .replace("\\[\\s*]".toRegex(), "[]")
    .indentByBrackets()
    .replace("""\n *\n""".toRegex(), "\n")
}

/**
 * shorthand for `toString().prettyToString()`, which adds line breaks and indents to a string
 *
 * @see prettyToString
 * @since 0.1.0
 */
internal fun Any?.toStringPretty(): String = when (this) {
  is Map<*, *> -> toList().joinToString("\n")
  else -> toString().prettyToString()
}

/**
 * A naive auto-indent which just counts brackets.
 *
 * @since 0.1.0
 */
internal fun String.indentByBrackets(tab: String = "  "): String {

  var tabCount = 0

  val open = setOf('{', '(', '[', '<')
  val close = setOf('}', ')', ']', '>')

  return lines()
    .map { it.trim() }
    .joinToString("\n") { line ->

      if (line.firstOrNull() in close) {
        tabCount--
      }

      "${tab.repeat(tabCount)}$line"
        .also {

          // Arrows aren't brackets
          val noSpecials = line.remove("<=", "->")

          tabCount += noSpecials.count { char -> char in open }
          // Skip the first char because if it's a closing bracket, it was already counted above.
          tabCount -= noSpecials.drop(1).count { char -> char in close }
        }
    }
}

/**
 * shorthand for `replace(___, "")` against multiple tokens
 *
 * @since 0.1.0
 */
internal fun String.remove(vararg strings: String): String = strings.fold(this) { acc, string ->
  acc.replace(string, "")
}

/**
 * shorthand for `replace(___, "")` against multiple tokens
 *
 * @since 0.1.0
 */
internal fun String.remove(vararg regex: Regex): String = regex.fold(this) { acc, reg ->
  acc.replace(reg, "")
}
