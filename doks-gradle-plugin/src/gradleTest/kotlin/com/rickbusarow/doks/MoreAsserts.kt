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

package com.rickbusarow.doks

import io.kotest.assertions.print.print
import io.kotest.matchers.EqualityMatcherResult
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.neverNullMatcher
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import java.io.File

interface MoreAsserts {

  infix fun File.shouldExistWithText(expectedText: String) {
    shouldExist()
    readText() shouldBe expectedText
  }

  infix fun File.shouldExistWithTextContaining(substring: String) {
    shouldExist()
    readText() shouldContain substring
  }

  infix fun String?.shouldContain(substring: String): String? {
    this should include(substring)
    return this
  }

  /**
   * This overloads Kotest's `include` so that it can return a different `MatcherResult`.
   * The [EqualityMatcherResult] results in a different exception when it fails,
   * which enables the 'click to see difference' feature in IntelliJ.
   * That diff is much more legible.
   */
  private fun include(substring: String) = neverNullMatcher<String> { actual ->
    EqualityMatcherResult.invoke(
      passed = actual.contains(substring),
      actual = actual,
      expected = substring,
      failureMessageFn = {
        "${actual.print().value} should include substring ${substring.print().value}"
      },
      negatedFailureMessageFn = {
        "${actual.print().value} should not include substring ${substring.print().value}"
      }
    )
  }
}
