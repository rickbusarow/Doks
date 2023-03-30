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

import org.intellij.lang.annotations.Language
import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownTokenTypes.Companion.HTML_BLOCK_CONTENT
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.LeafASTNode
import org.intellij.markdown.ast.getTextInNode
import org.intellij.markdown.flavours.MarkdownFlavourDescriptor
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.parser.MarkdownParser
import java.util.LinkedList

internal data class MarkdownNode(
  val node: ASTNode,
  private val fullText: String,
  val parent: MarkdownNode?
) {
  val isLeaf: Boolean get() = node is LeafASTNode
  val text by lazy { node.getTextInNode(fullText).toString() }
  val children: List<MarkdownNode> by lazy {
    node.children
      .map { child ->
        MarkdownNode(
          node = child,
          fullText = fullText,
          parent = this@MarkdownNode
        )
      }
  }

  val type: IElementType get() = node.type

  companion object {
    fun from(
      @Language("markdown") markdown: String,
      flavourDescriptor: MarkdownFlavourDescriptor
    ): MarkdownNode {
      return from(markdown, MarkdownParser(flavourDescriptor))
    }

    fun from(
      @Language("markdown") markdown: String,
      markdownParser: MarkdownParser = MarkdownParser(CommonMarkFlavourDescriptor())
    ): MarkdownNode = MarkdownNode(
      node = markdownParser.buildMarkdownTreeFromString(markdown),
      fullText = markdown,
      parent = null
    )
  }
}

internal fun MarkdownNode.isOpeningTag(): Boolean {

  if (type != HTML_BLOCK_CONTENT) {
    return false
  }

  return text.matches(openReg)
}

internal fun MarkdownNode.isClosingTag(): Boolean {

  if (type != HTML_BLOCK_CONTENT) {
    return false
  }

  return text.matches(closeReg)
}

internal fun MarkdownNode.depthFirst(): Sequence<MarkdownNode> {

  val toVisit = LinkedList(children)

  return generateSequence(toVisit::removeFirstOrNull) { node ->

    repeat(node.children.lastIndex + 1) {
      toVisit.addFirst(node.children[node.children.lastIndex - it])
    }
    toVisit.removeFirstOrNull()
  }
}

internal inline fun MarkdownNode.depthFirst(
  crossinline predicate: (MarkdownNode) -> Boolean
): Sequence<MarkdownNode> {

  val toVisit = LinkedList(children.filter(predicate))

  return generateSequence(toVisit::removeFirstOrNull) { node ->

    if (predicate(node)) {
      val filtered = node.children.filter(predicate)

      repeat(filtered.lastIndex + 1) {
        toVisit.addFirst(filtered[filtered.lastIndex - it])
      }

      toVisit.removeFirstOrNull()
    } else {
      null
    }
  }
}
