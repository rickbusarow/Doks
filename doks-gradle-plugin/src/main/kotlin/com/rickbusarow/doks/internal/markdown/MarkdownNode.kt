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

package com.rickbusarow.doks.internal.markdown

import com.rickbusarow.doks.internal.trees.depthFirstTraversal
import dev.drewhamilton.poko.Poko
import org.intellij.lang.annotations.Language
import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.LeafASTNode
import org.intellij.markdown.ast.getTextInNode
import org.intellij.markdown.flavours.MarkdownFlavourDescriptor
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.parser.MarkdownParser
import org.jetbrains.kotlin.com.intellij.openapi.util.Key
import org.jetbrains.kotlin.com.intellij.openapi.util.UserDataHolder

@Poko
internal class MarkdownNode(
  val node: ASTNode,
  private val fullText: String,
  val parent: MarkdownNode?
) : UserDataHolder, java.io.Serializable {
  val text: String by lazy { node.getTextInNode(fullText).toString() }

  val isLeaf: Boolean get() = node is LeafASTNode
  val isParagraph: Boolean get() = node.type == MarkdownElementTypes.PARAGRAPH
  val isHtmlBlock: Boolean get() = node.type == MarkdownElementTypes.HTML_BLOCK
  val isLeafOrParagraph: Boolean get() = isLeaf || isParagraph
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

  val elementType: IElementType get() = node.type

  private val _userData = mutableMapOf<Key<*>, Any?>()
  override fun <T : Any?> getUserData(key: Key<T>): T? {
    @Suppress("UNCHECKED_CAST")
    return _userData[key] as? T
  }

  override fun <T : Any?> putUserData(key: Key<T>, value: T?) {
    _userData[key] = value
  }

  companion object {
    private const val serialVersionUID: Long = -2219786698417908100L

    fun from(
      @Language("markdown") markdown: String,
      flavourDescriptor: MarkdownFlavourDescriptor
    ): MarkdownNode {
      return from(markdown, MarkdownParser(flavourDescriptor))
    }

    fun from(
      @Language("markdown") markdown: String,
      markdownParser: MarkdownParser = MarkdownParser(GFMFlavourDescriptor())
    ): MarkdownNode = MarkdownNode(
      node = markdownParser.buildMarkdownTreeFromString(markdown),
      fullText = markdown,
      parent = null
    )
  }
}

internal fun MarkdownNode?.isEOL(): Boolean = this != null && elementType == MarkdownTokenTypes.EOL

internal fun MarkdownNode.previousSiblings(): Sequence<MarkdownNode> {
  return parent?.children.orEmpty().asSequence().takeWhile { it != this }
}

internal fun MarkdownNode.previousSibling(): MarkdownNode? {
  return previousSiblings().lastOrNull()
}

internal fun MarkdownNode.nextSiblings(): Sequence<MarkdownNode> {
  return parent?.children.orEmpty().asSequence().dropWhile { it != this }.drop(1)
}

internal fun MarkdownNode.nextSibling(): MarkdownNode? {
  return nextSiblings().firstOrNull()
}

internal fun MarkdownNode.parents(): Sequence<MarkdownNode> = generateSequence(parent) { it.parent }

internal fun MarkdownNode.countParentLists() = parents()
  .count { it.elementType == MarkdownElementTypes.LIST_ITEM }

internal fun MarkdownNode?.isListItem(): Boolean {
  return this != null && elementType == MarkdownElementTypes.LIST_ITEM
}

internal fun MarkdownNode.isParagraphInListItem(): Boolean {
  return isParagraph && parent.isListItem()
}

internal fun MarkdownNode.isParagraphInBlockQuote(): Boolean {
  return isParagraph && parent.isBlockQuoteElement()
}

internal fun MarkdownNode.isFirstParagraphInParent(): Boolean {
  return isParagraph && previousSiblings().none { it.isParagraph }
}

internal fun MarkdownNode.isListItemDelimiter(): Boolean {
  return elementType == MarkdownTokenTypes.LIST_BULLET ||
    elementType == MarkdownTokenTypes.LIST_NUMBER
}

internal fun MarkdownNode.countParentBlockQuotes(): Int {
  return parents().count { it.isBlockQuoteElement() }
}

internal fun MarkdownNode?.isBlockQuoteElement(): Boolean {
  if (this == null) return false
  return elementType == MarkdownElementTypes.BLOCK_QUOTE
}

internal fun MarkdownNode?.isBlockQuoteToken(): Boolean {
  return when {
    this == null -> false
    !isLeaf -> false
    elementType == MarkdownTokenTypes.BLOCK_QUOTE -> true
    else -> isBlockQuoteTokenInWhiteSpace()
  }
}

/**
 * When a block quote is nested, any angle brackets other than the last one are
 * incorrectly parsed as WHITE_SPACE, and added as the last node of the parent block quote.
 *
 * @since 0.1.3
 */
internal fun MarkdownNode?.isBlockQuoteTokenInWhiteSpace(): Boolean {
  return this != null &&
    elementType == MarkdownTokenTypes.WHITE_SPACE &&
    text.matches("^>\\s&&[^\\n]+\$".toRegex())
}

internal fun MarkdownNode.isWhiteSpace(): Boolean {
  return elementType == MarkdownTokenTypes.WHITE_SPACE
}

internal fun MarkdownNode.isWhiteSpaceAfterNewLine(): Boolean {
  return isWhiteSpace() && previousSibling()?.elementType == MarkdownTokenTypes.EOL
}

internal fun MarkdownNode?.isEolFollowedByParagraph(): Boolean {
  return when {
    this == null -> false
    !isEOL() -> false
    else -> nextSiblings().any { it.isParagraph }
  }
}

internal fun MarkdownNode.firstChildOfTypeOrNull(vararg types: IElementType): MarkdownNode? {
  return childrenDepthFirst()
    .firstOrNull { it.elementType in types }
}

internal fun MarkdownNode.firstChildOfType(vararg types: IElementType): MarkdownNode {
  return childrenDepthFirst()
    .first { it.elementType in types }
}

internal fun MarkdownNode.childrenDepthFirst(): Sequence<MarkdownNode> {
  return depthFirstTraversal(MarkdownNode::children)
}

internal inline fun MarkdownNode.childrenDepthFirst(
  crossinline predicate: (MarkdownNode) -> Boolean
): Sequence<MarkdownNode> = depthFirstTraversal { children.filter(predicate) }
