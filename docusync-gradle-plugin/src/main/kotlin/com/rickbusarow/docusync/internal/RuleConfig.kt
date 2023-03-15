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

import com.rickbusarow.docusync.RuleName

internal data class RuleConfig(
  val name: RuleName,
  val minimumCount: Int,
  val maximumCount: Int
) {
  fun checkCount(matches: List<String>): Boolean {
    val actualCount = matches.size

    fun plural(count: Int) = when (count) {
      1 -> "match"
      else -> "matches"
    }

    check(minimumCount != maximumCount || actualCount == minimumCount) {
      "The rule '${name.value}' must find exactly $minimumCount ${plural(minimumCount)}, " +
        "but it found $actualCount."
    }

    check(minimumCount <= actualCount) {
      "The rule '${name.value}' must find a minimum of $minimumCount ${plural(minimumCount)}, " +
        "but it found $actualCount."
    }

    check(actualCount <= maximumCount) {
      "The rule '${name.value}' must find a maximum of $maximumCount ${plural(maximumCount)}, " +
        "but it found $actualCount."
    }

    return true
  }
}
