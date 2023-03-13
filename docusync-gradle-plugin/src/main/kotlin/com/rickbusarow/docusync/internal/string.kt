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

package com.rickbusarow.docusync.internal

import java.util.Locale

/**
 * Replaces the deprecated Kotlin version, but hard-codes `Locale.US`
 */
fun String.capitalize(): String = replaceFirstChar {
  if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString()
}

/**
 * Removes trailing whitespaces from all lines in a string.
 *
 * Shorthand for `lines().joinToString("\n") { it.trimEnd() }`
 */
fun String.trimLineEnds(): String = mapLines { it.trimEnd() }

/**
 * performs [transform] on each line
 */
fun String.mapLines(
  transform: (String) -> CharSequence
): String = lineSequence()
  .joinToString("\n", transform = transform)

/**
 * shorthand for `joinToString("") { ... }`
 */
fun <E> Sequence<E>.joinToStringConcat(
  transform: ((E) -> CharSequence)? = null
): String = joinToString("", transform = transform)

/**
 * shorthand for `joinToString("") { ... }`
 */
fun <E> Iterable<E>.joinToStringConcat(
  transform: ((E) -> CharSequence)? = null
): String = joinToString("", transform = transform)

/**
 * Converts all line separators in the receiver string to use `\n`.
 */
fun String.normaliseLineSeparators(): String = replace("\r\n|\r".toRegex(), "\n")

/**
 * adds [prefix] to the beginning of the receiver string if the string does not already start with that
 * prefix.
 */
fun String.prefixIfNot(prefix: String): String {
  return if (this.startsWith(prefix)) this else "$prefix$this"
}

/**
 * Adds line breaks and indents to the output of data class `toString()`s.
 *
 * @see toStringPretty
 */
fun String.prettyToString(): String {
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
 */
fun Any?.toStringPretty(): String = when (this) {
  is Map<*, *> -> toList().joinToString("\n")
  else -> toString().prettyToString()
}

/**
 * A naive auto-indent which just counts brackets.
 */
fun String.indentByBrackets(tab: String = "  "): String {

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
 */
fun String.remove(vararg strings: String): String = strings.fold(this) { acc, string ->
  acc.replace(string, "")
}

/**
 * @param uppercase `true` for `SCREAMING_SNAKE_CASE`, `false` for `snake_case`
 * @return a `snake_case` or `SNAKE_CASE` version of the receiver string where the original capital
 *     letters are preceded by an underscore and all cases are uniform
 */
fun String.snakeCase(uppercase: Boolean = false): String {
  val camelRegex = "(?<=[a-zA-Z])[A-Z]".toRegex()
  return camelRegex.replace(this) { "_${it.value}" }
    .let {
      if (uppercase) {
        it.uppercase()
      } else {
        it.lowercase()
      }
    }
}

/**
 * Finds the maximum common prefix in the list. If [delimiter] is provided, the string is truncated
 * after the last occurrence, but the last occurrence itself is included.
 *
 * ```
 * listOf(
 *   "foo/bar/baz/file1.txt",
 *   "foo/bar/baz/file2.txt",
 *   "foo/bar/boz/file3.txt"
 * ).commonPrefix("/") == "foo/bar/"
 * ```
 */
fun List<String>.commonPrefix(delimiter: String? = null): String {
  if (isEmpty()) return ""
  if (size == 1) return first()

  return asSequence()
    .drop(1)
    .letIf(delimiter != null) {
      map { it.substringBeforeLast(delimiter!!) }
    }
    .fold(first()) { common, str ->
      common.commonPrefixWith(str)
    }
}

/**
 * Returns a substring before and including the last occurrence of [delimiter]. If the string does not
 * contain the delimiter, returns [missingDelimiterValue] which defaults to the original string.
 *
 * "foo/bar/baz/file.txt".substringUpToLast('/') == "foo/bar/baz/"
 */
fun String.substringUpToLast(
  delimiter: Char,
  missingDelimiterValue: String = this
): String {
  val index = lastIndexOf(delimiter)
  return if (index == -1) missingDelimiterValue else substring(0, index + 1)
}

/**
 * Returns a substring before and including the last occurrence of [delimiter]. If the string does not
 * contain the delimiter, returns [missingDelimiterValue] which defaults to the original string.
 *
 * "foo/bar/baz/file.txt".substringUpToLast("/") == "foo/bar/baz/"
 */
fun String.substringUpToLast(
  delimiter: String,
  missingDelimiterValue: String = this
): String {
  val index = lastIndexOf(delimiter)
  return if (index == -1) missingDelimiterValue else substring(0, index + 1)
}

/**
 * code golf for ` takeIf { it.isNotEmpty() }`
 *
 * @since 0.10.5
 */
fun String.takeIfNotEmpty(): String? = takeIf { it.isNotEmpty() }

/**
 * code golf for ` takeIf { it.isNotBlank() }`
 *
 * @since 0.10.5
 */
fun String.takeIfNotBlank(): String? = takeIf { it.isNotBlank() }
