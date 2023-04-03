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

package com.rickbusarow.doks.internal.markdown

import com.rickbusarow.doks.internal.Rule
import com.rickbusarow.doks.internal.RuleName
import com.rickbusarow.doks.internal.Rules
import com.rickbusarow.doks.internal.stdlib.Color.Companion.colorized
import com.rickbusarow.doks.internal.stdlib.Color.LIGHT_GREEN
import com.rickbusarow.doks.internal.stdlib.Color.LIGHT_YELLOW
import com.rickbusarow.doks.internal.stdlib.SEMVER_REGEX
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrowWithMessage
import io.kotest.matchers.shouldBe
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test

class MarkdownTest {

  val rules = Rules(
    Rule(
      name = "doks-maven",
      regex = """(com.rickbusarow.doks:[^:]*?doks[^:]*?:)$SEMVER_REGEX""",
      replacement = "$11.2.3"
    ),
    Rule(
      name = "cats",
      regex = """cats""",
      replacement = "dogs"
    )
  )

  @Test
  fun `replacement works`() {

    rules[RuleName("doks-maven")]

    val original = md(
      """
      # Title
      <!--doks doks-maven:1-->

      com.rickbusarow.doks:doks-cli:0.0.1-SNAPSHOT

      <!--/doks-->

      <!--doks doks-maven:1-->

      'com.rickbusarow.doks:doks-cli:0.0.1-SNAPSHOT'

      <!--/doks-->

      fin
      """
    )

    val new = original.markdown(
      absolutePath = "foo.md",
      rules = rules,
      autoCorrect = true
    )

    new shouldBe md(
      """
      # Title
      <!--doks doks-maven:1-->

      com.rickbusarow.doks:doks-cli:1.2.3

      <!--/doks-->

      <!--doks doks-maven:1-->

      'com.rickbusarow.doks:doks-cli:1.2.3'

      <!--/doks-->

      fin
      """
    )
  }

  @Test
  fun `replacement fails if autoCorrect is false`() {

    val original = md(
      """
      # Title
      <!--doks doks-maven:1-->

      com.rickbusarow.doks:doks-cli:0.0.1-SNAPSHOT

      <!--/doks-->

      <!--doks doks-maven:1-->

      'com.rickbusarow.doks:doks-cli:0.0.1-SNAPSHOT'

      <!--/doks-->

      fin
      """
    )

    shouldThrowWithMessage<IllegalStateException>(
      """
        |Doks - file://foo.md > text is out of date.
        |
        |line 4   ${"--  com.rickbusarow.doks:doks-cli:0.0.1-SNAPSHOT".colorized(LIGHT_YELLOW)}
        |         ${"++  com.rickbusarow.doks:doks-cli:1.2.3".colorized(LIGHT_GREEN)}
        |line 10  ${"--  'com.rickbusarow.doks:doks-cli:0.0.1-SNAPSHOT'".colorized(LIGHT_YELLOW)}
        |         ${"++  'com.rickbusarow.doks:doks-cli:1.2.3'".colorized(LIGHT_GREEN)}
        |""".trimMargin()
    ) {
      original.markdown(
        absolutePath = "foo.md",
        rules = rules,
        autoCorrect = false
      )
    }
  }

  @Test
  fun `a tag must be closed before opening another tag`() {

    val original = md(
      """
      # Title
      <!--doks doks-maven:1-->

      com.rickbusarow.doks:doks-cli:0.0.1-SNAPSHOT

      <!--doks doks-maven:1-->

      'com.rickbusarow.doks:doks-cli:0.0.1-SNAPSHOT'

      <!--/doks-->

      fin
      """
    )

    shouldThrowWithMessage<IllegalStateException>(
      "Doks - file://foo.md:1:0 > The tag '<!--doks doks-maven:1-->' " +
        "must be closed with `<!--/doks-->` before the next doks opening tag."
    ) {

      original.markdown(
        absolutePath = "foo.md",
        rules = rules,
        autoCorrect = true
      )
    }
  }

  @Test
  fun `a tag does not need to be closed if there are no more tags after it`() {

    val original = md(
      """
      # Title
      <!--doks doks-maven:1-->

      com.rickbusarow.doks:doks-cli:0.0.1-SNAPSHOT

      fin
      """
    )

    shouldNotThrowAny {
      original.markdown(
        absolutePath = "foo.md",
        rules = rules,
        autoCorrect = true
      )
    }
  }

  @Test
  fun `a rule with only one number must match that number of times`() {

    val original = md(
      """
      # Title
      <!--doks doks-maven:1-->

      com.rickbusarow.doks:doks-cli:0.0.1-SNAPSHOT
      com.rickbusarow.doks:doks-cli:0.0.1-SNAPSHOT

      fin
      """
    )
    shouldThrowWithMessage<java.lang.IllegalStateException>(
      "The rule 'doks-maven' must find exactly 1 match, but it found 2."
    ) {
      original.markdown(
        absolutePath = "foo.md",
        rules = rules,
        autoCorrect = true
      )
    }
  }

  @Test
  fun `a rule with a range cannot be above that range`() {

    val original = md(
      """
      # Title
      <!--doks doks-maven:1-2-->

      com.rickbusarow.doks:doks-cli:0.0.1-SNAPSHOT
      com.rickbusarow.doks:doks-cli:0.0.1-SNAPSHOT
      com.rickbusarow.doks:doks-cli:0.0.1-SNAPSHOT

      fin
      """
    )
    shouldThrowWithMessage<java.lang.IllegalStateException>(
      "The rule 'doks-maven' must find a maximum of 2 matches, but it found 3."
    ) {
      original.markdown(
        absolutePath = "foo.md",
        rules = rules,
        autoCorrect = true
      )
    }
  }

  @Test
  fun `a rule with a range cannot be below that range`() {

    val original = md(
      """
      # Title
      <!--doks doks-maven:2-3-->

      com.rickbusarow.doks:doks-cli:0.0.1-SNAPSHOT

      fin
      """
    )
    shouldThrowWithMessage<java.lang.IllegalStateException>(
      "The rule 'doks-maven' must find a minimum of 2 matches, but it found 1."
    ) {
      original.markdown(
        absolutePath = "foo.md",
        rules = rules,
        autoCorrect = true
      )
    }
  }

  fun md(@Language("markdown") content: String): String = content.trimIndent()
}
