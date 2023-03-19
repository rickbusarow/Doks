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

package builds.ktlint.rules

import com.pinterest.ktlint.core.RuleProvider
import com.pinterest.ktlint.test.lint
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class KDocLeadingAsteriskRuleTest {

  val rules = setOf(
    RuleProvider { KDocLeadingAsteriskRule() }
  )

  @Test
  fun `asterisks are added to the default section`() {

    rules.format(
      """
      /**
       extercitatrekvsuion nostrud exerc mco laboris nisteghi ut aliquip ex ea
       * desegrunt fugiat nulla pariatur. Excepteur sint occaecat cupidatat



       @property name the name property
       @property age a number, probably



       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    ) shouldBe """
      /**
       * extercitatrekvsuion nostrud exerc mco laboris nisteghi ut aliquip ex ea
       * desegrunt fugiat nulla pariatur. Excepteur sint occaecat cupidatat
       *
       *
       *
       * @property name the name property
       * @property age a number, probably
       *
       *
       *
       */
      data class Subject(
        val name: String,
        val age: Int
      )
    """.trimIndent()
  }

  @Test
  fun `a single blank line after a tag and before kdoc end is fixed`() {

    rules.format(
      """
      /**
       * @property age a number, probably

       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    ) shouldBe """
      /**
       * @property age a number, probably
       *
       */
      data class Subject(
        val name: String,
        val age: Int
      )
    """.trimIndent()
  }

  @Test
  fun `a single blank line before kdoc end is fixed`() {

    rules.format(
      """
      /**
       * extercitatrekvsuion nostrud exerc mco laboris nisteghi ut aliquip ex ea

       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    ) shouldBe """
      /**
       * extercitatrekvsuion nostrud exerc mco laboris nisteghi ut aliquip ex ea
       *
       */
      data class Subject(
        val name: String,
        val age: Int
      )
    """.trimIndent()
  }

  @Test
  fun `a kdoc with all its asterisks is left alone`() {

    rules.lint(
      """
      /**
       * extercitatrekvsuion nostrud exerc mco laboris nisteghi ut  laboris nisteghi ut liquip ex ea
       * fugiat nulla para tur. Excepteur sipteur sint
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    ) shouldBe emptyList()
  }

  @Test
  fun `a kdoc with all its asterisks and tags is left alone`() {

    rules.lint(
      """
      /**
       * extercitatrekvsuion nostrud exerc mco laboris nisteghi ut aliquip ex ea
       * desegrunt fugiat nulla pariatur. Excepteur sint occaecat cupidatat
       *
       *
       *
       * @property name the name property
       * @property age a number, probably
       *
       *
       *
       */
      data class Subject(
        val name: String,
        val age: Int
      )
      """.trimIndent()
    ) shouldBe emptyList()
  }
}
