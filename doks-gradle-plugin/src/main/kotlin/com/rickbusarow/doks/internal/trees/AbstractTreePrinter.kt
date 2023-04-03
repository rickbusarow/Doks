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

package com.rickbusarow.doks.internal.trees

internal abstract class AbstractTreePrinter<T : Any>(
  private val whitespaceChar: Char = ' '
) {
  private val levels = mutableMapOf<T, Int>()
  private val dashes = "------------------------------------------------------------"

  private val parentNameMap = mutableMapOf<T, String>()

  abstract fun T.simpleClassName(): String
  abstract fun T.parent(): T?
  abstract fun T.typeName(): String
  abstract fun T.text(): String

  abstract fun depthFirstChildren(root: T): Sequence<T>

  fun visitRoot(rootNode: T) {

    depthFirstChildren(rootNode)
      .forEach { node ->

        val thisName = node.simpleClassName()
        val parentName = (node.parentName() ?: "----")

        val parentLevel = node.parent()?.let { parent -> levels[parent] } ?: 0
        levels[node] = parentLevel + 1

        printNode(
          elementSimpleName = thisName,
          elementType = node.typeName(),
          parentName = parentName,
          parentType = node.parent()?.typeName() ?: "----",
          nodeText = node.text().replace(" ", "$whitespaceChar"),
          level = parentLevel + 1
        )
      }
  }

  private fun printNode(
    elementSimpleName: String,
    elementType: String,
    parentName: String,
    parentType: String,
    nodeText: String,
    level: Int
  ) {
    println(
      buildString {
        append("   $dashes")
        append("   $elementSimpleName")
        append("   -- type: $elementType")
        append("   -- parent: $parentName")
        append("   -- parent type: $parentType")
        append('\n')
        append('\n')
        append(
          "   %|$nodeText".replaceIndentByMargin(
            newIndent = "",
            marginPrefix = "%|"
          ).prependIndent("│")
        )
        append('\n')
      }
        .lines()
        .let {
          it.dropLast(1) + it.last().replaceFirst("  ", "└─")
        }
        .joinToString("\n")
        .prependIndent("│   ".repeat(level))
    )
  }

  private fun T.parentName() = parent()?.let { parent ->

    parentNameMap.getOrPut(parent) {
      val typeCount = parentNameMap.keys.count { it.typeName() == parent.typeName() }

      val simpleName = parent.typeName()

      val start = if (typeCount == 0) {
        simpleName
      } else {
        "$simpleName (${typeCount + 1})"
      }

      start
    }
  }
}
