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
import com.rickbusarow.docusync.internal.stdlib.SEMVER_REGEX
import com.rickbusarow.docusync.internal.stdlib.SEMVER_REGEX_STABLE
import com.rickbusarow.docusync.internal.stdlib.requireNotNull
import com.rickbusarow.docusync.psi.SampleRequest
import org.gradle.api.Named
import org.intellij.lang.annotations.Language

/**
 * Models a single replacement action very much like the [Regex] version of [String.replace].
 *
 * @since 0.1.0
 */
@Suppress("MemberVisibilityCanBePrivate", "PropertyName", "VariableNaming")
abstract class RuleBuilderScope : Named, java.io.Serializable {

  @PublishedApi
  internal val sourceDelim: Char = '​'

  /**
   * A regex which matches words separated by '.'.
   *
   * ex: `com.example.library`
   *
   * @since 0.1.0
   */
  val GROUP_ID: String
    get() = "[\\w\\.]*[\\w]"

  /**
   * A regex which matches words separated by '-'.
   *
   * ex: `lifecycle-ktx`
   *
   * @since 0.1.0
   */
  val ARTIFACT_ID: String
    get() = "[\\w\\-]*[\\w]"

  /**
   * ex: `aar`
   *
   * @since 0.1.0
   */
  val PACKAGING: String
    get() = "\\w+"

  /**
   * from https://ihateregex.io/expr/semver/ but without capturing groups
   *
   * ex: `1.0.0-SNAPSHOT`
   *
   * @see SEMVER_REGEX
   * @since 0.1.0
   */
  val SEMVER: String
    get() = SEMVER_REGEX

  /**
   * matches "stable" versions only, like `1.0.0` or `0.10.0`. It
   * does not match pre-release versions like `1.0.0-SNAPSHOT`.
   *
   * @see SEMVER_REGEX_STABLE
   * @since 0.1.0
   */
  val SEMVER_STABLE: String
    get() = SEMVER_REGEX_STABLE

  /**
   * supports normal Regex semantics including capturing groups like `(.*)`
   *
   * @since 0.1.0
   */
  @setparam:Language("regexp")
  abstract var regex: String?

  /**
   * any combination of literal text and $-substitutions
   *
   * @since 0.1.0
   */
  abstract var replacement: String

  private val _sampleRequests: MutableList<SampleRequest> = mutableListOf()
  internal val sampleRequests: List<SampleRequest>
    get() = _sampleRequests

  internal fun requireRegex(): String {
    return regex.requireNotNull { "A regex value was not for the rule $name" }
  }

  /**
   * Creates a replacement string of a Markdown code block, where
   * the sample code is taken from the declaration of [fqName].
   *
   * The replacement string will have a format of:
   *
   *       ```$codeBlockLanguage $attributes
   *       // sample code from fqName
   *     ```
   *
   * ### the bodyOnly flag
   *
   * If [bodyOnly] is true, the parsed sample code will only include
   * the body of a declaration instead of the declaration itself.
   *
   * Consider this function:
   *
   * ```
   * fun foo() {
   *   doSomething()
   *   doSomethingElse()
   * }
   * ```
   *
   * If `bodyOnly` is true, the sample code will only include the "body" -- the middle two lines.
   * The replacement string will also use [trimIndent] so that everything is left-justified as you
   * would expect. If `bodyOnly` is false, the replacement string will be all four lines of code.
   *
   * ### sample string literals
   *
   * When the sample code is a string property declaration, you may
   * use `bodyOnly` to extract the value of a raw string literal.
   *
   * Given this string property:
   *
   * ```
   * val groovyConfig = """
   *   // build.gradle
   *   anvil {
   *     generateDaggerFactories = true
   *   }
   *   """.trimIndent()
   * ```
   *
   * The resultant code sample using `bodyOnly = true` would look like this:
   *
   *       ```groovy
   *       // build.gradle
   *       anvil {
   *         generateDaggerFactories = true
   *       }
   *     ```
   *
   * @since 0.1.0
   */
  fun sourceCode(
    fqName: String,
    bodyOnly: Boolean = false,
    codeBlockLanguage: String = "kotlin",
    attributes: String? = null,
  ): String {

    val request = SampleRequest(fqName = fqName, bodyOnly = bodyOnly)

    _sampleRequests.add(request)

    val fenceOpen = if (!attributes.isNullOrBlank()) {
      "```$codeBlockLanguage $attributes"
    } else {
      "```$codeBlockLanguage"
    }

    if (regex == null) {
      // match a code block which has the same opening fence as this one, any content,
      // and then a closing fence with nothing else on that line
      regex = "^[\\s\\S]+\$"
    }

    return buildString {
      appendLine()
      appendLine()
      appendLine(fenceOpen)

      appendLine("$sourceDelim${request.hashCode()}$sourceDelim")
      appendLine("```")
      appendLine()
    }
  }

  /**
   * Builds a [Regex] pattern to match a maven artifact as commonly defined in a Gradle project.
   *
   * An example match: `com.example.foo:foo-utils:1.2.3-SNAPSHOT`
   *
   * ### Grouping
   *
   * The resultant regex has four capturing groups, as in: `"(($group):($artifactId):($version))"`
   *
   * When this regex matches, `$1` would contain the group,
   * `$2` the artifact ID, and `$3` would contain the version.
   *
   * @param group matcher for the group component, like `com.example.myLib`.
   *   The default value of `[\w\.]*[\w]` will match any group.
   * @param artifactId matcher for the "artifactId" or "name" component, like
   *   `myLib-api`. The default value of `[\w\-]*[\w]` will match any artifact id.
   * @param version matcher for the version component, like `1.1.0` or
   *   `1.0.0-SNAPSHOT`. The default value will match any semantic version.
   * @return a regex string to match any maven artifact
   * @since 0.1.0
   */
  fun maven(
    @Language("regexp") group: String = GROUP_ID,
    @Language("regexp") artifactId: String = ARTIFACT_ID,
    @Language("regexp") version: String = SEMVER
  ): String = "($group):($artifactId):($version)"

  /**
   * Builds a regular expression pattern to match a Gradle
   * plugin declaration that uses the given plugin ID.
   *
   * The regular expression will match against the following formats of a plugin declaration:
   *
   * ```
   * plugins {
   *   id("com.example.myPlugin")
   *   id 'com.example.myPlugin'
   *   id("com.example.myPlugin") version "0.0.1"
   *   id("com.example.myPlugin").version("0.0.1")
   *   id 'com.example.myPlugin' version '0.0.1'
   * }
   * ```
   *
   * ### Grouping
   *
   * The resultant regex will have three capturing groups:
   *
   * - Capture 1: The "id" invocation and everything before the plugin ID argument, like `id("`
   * - Capture 2: The matched plugin ID
   * - Capture 3: The closing single/double quote and optional parenthesis, like `")`
   *
   * If the [pluginId] argument has its own capture groups, the group numbers will be affected.
   *
   * @param pluginId the plugin ID to match. This may be a regex pattern.
   * @return a regex string to match the given plugin ID in a Gradle plugin declaration
   * @since 0.1.0
   */
  fun gradlePlugin(@Language("regexp") pluginId: String): String {

    //language=regexp
    return buildString {
      // Don't include it in the match, but make sure that `id` is either at the start of a word
      // boundary or it's the absolute start of the string.
      append("""(?<=\b)""")
      // Capture 1 - the "id" invocation and everything before the plugin id argument, like `id("`
      append("""(id *(?:\("|\('| '))""")
      // Capture 2 - the plugin ID match
      append("($pluginId)")
      // Capture 3 - the closing single/double quote and optional parenthesis. If the match ends with
      // just a single quote, use a look-ahead to make sure it's followed by a whitespace or
      // end-of-string.
      append("""("\)|'\)|'(?=\b|\s|${'$'}))""")
    }
  }

  /**
   * Builds a regex pattern to match a Gradle plugin declaration
   * that specifies both the plugin ID and the plugin version.
   *
   * The pattern matches the standard plugin declaration syntax in Gradle build files, such as:
   *
   * ```
   * plugins {
   *   id("com.example.myPlugin") version "1.2.3"
   *   id("com.example.myPlugin").version("1.2.3")
   *   id 'com.example.myPlugin' version '1.2.3'
   *   id('com.example.myPlugin') version '1.2.3'
   * }
   * ```
   *
   * ### Grouping
   *
   * The resultant regex will have six capturing groups:
   *
   * - Capture 1: The "id" invocation and everything before the plugin ID argument, like `id("`
   * - Capture 2: The matched plugin ID
   * - Capture 3: The closing single/double quote and optional parenthesis, like `")`
   * - Capture 4: The invocation of the "version" function, like `version("`
   * - Capture 5: The matched plugin version
   * - Capture 6: The closing single/double quote and optional parenthesis, like `")`
   *
   * If the [pluginId] or [version] arguments have their
   * own capture groups, the group numbers will be affected.
   *
   * @param pluginId the plugin ID to match. This may be a regex pattern.
   * @param version the version of the plugin to match. The default value
   *   will match any semantic version. This may be a regex pattern.
   * @return a regex string to match a Gradle plugin declaration with both ID and version specified
   * @since 0.1.0
   */
  fun gradlePluginWithVersion(
    @Language("regexp") pluginId: String,
    @Language("regexp") version: String = SEMVER
  ): String {

    //language=regexp
    return buildString {
      append(gradlePlugin(pluginId))
      // Capture 4 - the invocation of the "version" function, including the opening quote
      append("( *\\.?version(?:\\(\"|\\('| +\"| +'))")
      // Capture 5 - the plugin version match
      append("($version)")
      // Capture 6 - whatever is used after the version: "")", "')", or just "'".
      append("(\"\\)|'\\)|'|\")(?=\\b|\\s|${'$'})")
    }
  }

  /**
   * An inline version of [Regex.escape]. The receiver string will be interpreted
   * as a literal string. No characters within the string will have special meaning.
   *
   * @since 0.1.1
   */
  fun String.escapeRegex(): String = Regex.escape(this)

  /**
   * An inline version of [Regex.escapeReplacement]. The receiver string will be interpreted
   * as a literal string. No characters within the string will have special meaning.
   *
   * @since 0.1.1
   */
  fun String.escapeReplacement(): String = Regex.escapeReplacement(this)

  /**
   * @return a [Rule] from the current values of [regex] and [replacement]
   * @since 0.1.0
   */
  internal fun toRule(): Rule = Rule(
    name = name,
    regex = requireRegex(),
    replacement = replacement
  )
}
