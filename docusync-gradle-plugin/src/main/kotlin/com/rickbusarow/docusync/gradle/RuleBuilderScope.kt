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
import com.rickbusarow.docusync.internal.SEMVER_REGEX
import com.rickbusarow.docusync.internal.SEMVER_REGEX_STABLE
import com.rickbusarow.docusync.internal.requireNotNull
import com.rickbusarow.docusync.psi.SampleRequest
import org.gradle.api.Named
import org.intellij.lang.annotations.Language

/** Models a single replacement action very much like the [Regex] version of [String.replace] */
abstract class RuleBuilderScope : Named, java.io.Serializable {

  @PublishedApi
  internal val sourceDelim: Char = '​'

  /**
   * A regex which matches words separated by '.'.
   *
   * ex: `com.example.library`
   */
  @Suppress("VariableNaming")
  val GROUP_ID: String
    get() = "[\\w\\.]*[\\w]"

  /**
   * A regex which matches words separated by '-'.
   *
   * ex: `lifecycle-ktx`
   */
  @Suppress("VariableNaming")
  val ARTIFACT_ID: String
    get() = "[\\w\\-]*[\\w]"

  /**
   * ex: `aar`
   */
  @Suppress("VariableNaming")
  val PACKAGING: String
    get() = "\\w+"

  /**
   * from https://ihateregex.io/expr/semver/ but without capturing groups
   *
   * ex: `1.0.0-SNAPSHOT`
   * @see SEMVER_REGEX
   */
  @Suppress("VariableNaming")
  val SEMVER: String
    get() = SEMVER_REGEX

  /**
   * matches "stable" versions only, like `1.0.0` or `0.10.0`. It does not match pre-release versions
   * like `1.0.0-SNAPSHOT`.
   *
   * @see SEMVER_REGEX_STABLE
   */
  @Suppress("VariableNaming")
  val SEMVER_STABLE: String
    get() = SEMVER_REGEX_STABLE

  /** supports normal Regex semantics including capturing groups like `(.*)` */
  @setparam:Language("regexp")
  abstract var regex: String?

  /** any combination of literal text and $-substitutions */
  abstract var replacement: String

  private val _sampleRequests: MutableList<SampleRequest> = mutableListOf()
  internal val sampleRequests: List<SampleRequest>
    get() = _sampleRequests

  internal fun requireRegex(): String {
    return regex.requireNotNull { "A regex value was not for the rule $name" }
  }

  /**
   * Creates a replacement string of a Markdown code block, where the sample code is taken from the
   * declaration of [fqName].
   *
   * The replacement string will have a format of:
   *
   *     ```$codeBlockLanguage $attributes
   *     // sample code from fqName
   *     ```
   *
   * ### the bodyOnly flag
   *
   * If [bodyOnly] is true, the parsed sample code will only include the body of a declaration instead
   * of the declaration itself.
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
   * If `bodyOnly` is true, the sample code will only include the "body" -- the middle two lines. The
   * replacement string will also use [trimIndent] so that everything is left-justified as you would
   * expect. If `bodyOnly` is false, the replacement string will be all four lines of code.
   *
   * ### sample string literals
   *
   * When the sample code is a string property declaration, you may use `bodyOnly` to extract the value
   * of a raw string literal.
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
   *     ```groovy
   *     // build.gradle
   *     anvil {
   *       generateDaggerFactories = true
   *     }
   *     ```
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
   * When this regex matches, `$1` would contain the group, `$2` the artifact ID, and `$3` would contain
   * the version.
   *
   * @param group matcher for the group component, like `com.example.myLib`. The default value of
   *   `[\w\.]*[\w]` will match any group.
   * @param artifactId matcher for the "artifactId" or "name" component, like `myLib-api`. The default
   *   value of `[\w\-]*[\w]` will match any artifact id.
   * @param version matcher for the version component, like `1.1.0` or `1.0.0-SNAPSHOT`. The default
   *   value will match any semantic version.
   * @return a regex string to match any maven artifact
   */
  fun maven(
    group: String = GROUP_ID,
    artifactId: String = ARTIFACT_ID,
    version: String = SEMVER
  ): String = "(($group):($artifactId):($version))"

  /** @return a [Rule] from the current values of [regex] and [replacement] */
  internal fun toRule(): Rule = Rule(
    name = name,
    regex = requireRegex(),
    replacement = replacement
  )
}
