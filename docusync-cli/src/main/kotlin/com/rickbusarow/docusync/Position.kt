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

package com.rickbusarow.docusync

/**
 * @property row starts at 0
 * @property column starts at 0
 */
data class Position(
  val row: Int,
  val column: Int
) : Comparable<Position> {

  override fun compareTo(other: Position): Int {
    return row.compareTo(other.row)
  }

  companion object {

    /**
     * @return the [Position] of the next [token] after [startIndex]
     */
    fun String.positionOfSubstring(token: String, startIndex: Int): Position {

      val lines = lines()

      val totalIndex = indexOf(token.trimStart(), startIndex = startIndex)
      var rowIndex = totalIndex

      var row = 0

      while (lines[row].length < rowIndex) {
        // if the current row's string isn't long enough, subtract its length from the total index
        // and move on to the next row.  Subtract an additional 1 because the newline character
        // in the full string isn't included in the line's string.
        rowIndex -= (lines[row].length + 1)
        row++
      }
      return Position(row, rowIndex)
    }
  }
}
