/*
 * Copyright (C) 2025 Rick Busarow
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

import com.rickbusarow.doks.internal.markdown.MarkdownNode
import org.jetbrains.kotlin.com.intellij.openapi.util.UserDataHolder

internal inline fun <T : UserDataHolder> T.depthFirstTraversal(
  crossinline childrenFactory: T.() -> List<T>
): Sequence<T> = depthFirstTraversalPrivate(childrenFactory)

internal inline fun MarkdownNode.depthFirstTraversal(
  crossinline childrenFactory: MarkdownNode.() -> List<MarkdownNode>
): Sequence<MarkdownNode> = depthFirstTraversalPrivate(childrenFactory)

private inline fun <T> T.depthFirstTraversalPrivate(
  crossinline childrenFactory: T.() -> List<T>
): Sequence<T> {
  val stack = ArrayDeque<T>()
  stack.addLast(this)

  return generateSequence {
    stack.removeLastOrNull()
      ?.also { current ->
        val children = childrenFactory(current)
        when (children.size) {
          0 -> Unit // Do nothing for empty children
          1 -> stack.addLast(children[0]) // Add the only child directly
          else -> stack.addAll(children.asReversed()) // Add all the children in reversed order
        }
      }
  }
}

internal inline fun <T : UserDataHolder> T.breadthFirstTraversal(
  crossinline childrenFactory: T.() -> List<T>
): Sequence<T> = breadthFirstTraversalPrivate(childrenFactory)

internal inline fun MarkdownNode.breadthFirstTraversal(
  crossinline childrenFactory: MarkdownNode.() -> List<MarkdownNode>
): Sequence<MarkdownNode> = breadthFirstTraversalPrivate(childrenFactory)

private inline fun <T> T.breadthFirstTraversalPrivate(
  crossinline childrenFactory: T.() -> List<T>
): Sequence<T> {
  val queue = ArrayDeque<T>()
  queue.addLast(this)

  return generateSequence {
    queue.removeFirstOrNull()
      ?.also { current ->
        val children = childrenFactory(current)
        when (children.size) {
          0 -> Unit // Do nothing for empty children
          1 -> queue.addLast(children[0]) // Add the only child directly
          else -> queue.addAll(children.asReversed()) // Add all the children in reversed order
        }
      }
  }
}
