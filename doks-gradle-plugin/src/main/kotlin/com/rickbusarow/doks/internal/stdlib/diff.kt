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

package com.rickbusarow.doks.internal.stdlib

import com.github.difflib.text.DiffRow.Tag
import com.github.difflib.text.DiffRowGenerator
import com.rickbusarow.doks.internal.stdlib.Color.Companion.colorized
import com.rickbusarow.doks.internal.stdlib.Color.LIGHT_GREEN
import com.rickbusarow.doks.internal.stdlib.Color.LIGHT_YELLOW

internal fun diffString(oldStr: String, newStr: String): String {

  return buildString {

    val rows = DiffRowGenerator.create()
      .showInlineDiffs(true)
      // .mergeOriginalRevised(true)
      .inlineDiffByWord(true)
      .oldTag { _: Boolean? -> "" }
      .newTag { _: Boolean? -> "" }
      .build()
      .generateDiffRows(oldStr.lines(), newStr.lines())

    val linePadding = rows.size.toString().length + 1

    rows.forEachIndexed { line, diffRow ->
      if (diffRow.tag != Tag.EQUAL) {
        append("line ${line.inc().toString().padEnd(linePadding)} ")
        appendLine("--  ${diffRow.oldLine}".colorized(LIGHT_YELLOW))
        append("      " + " ".repeat(linePadding))
        appendLine("++  ${diffRow.newLine}".colorized(LIGHT_GREEN))
      }
    }
  }
}

/**
 * https://github.com/ziggy42/kolor
 *
 * @property code
 * @since 0.1.3
 */
@Suppress("MagicNumber")
internal enum class Color(val code: Int) {
  BLACK(30),
  RED(31),
  GREEN(32),
  YELLOW(33),
  BLUE(34),
  MAGENTA(35),
  CYAN(36),
  LIGHT_GRAY(37),
  DARK_GRAY(90),
  LIGHT_RED(91),
  LIGHT_GREEN(92),
  LIGHT_YELLOW(93),
  LIGHT_BLUE(94),
  LIGHT_MAGENTA(95),
  LIGHT_CYAN(96),
  WHITE(97);

  companion object {

    private val supported = "win" !in System.getProperty("os.name").lowercase()

    /**
     * returns a string in the given color
     *
     * @since 0.1.0
     */
    fun String.colorized(color: Color) = if (supported) {
      "\u001B[${color.code}m$this\u001B[0m"
    } else {
      this
    }
  }
}
