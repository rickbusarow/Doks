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

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType
import com.pinterest.ktlint.core.ast.ElementType.KDOC_END
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import com.pinterest.ktlint.core.ast.isWhiteSpace
import com.pinterest.ktlint.core.ast.isWhiteSpaceWithNewline
import com.pinterest.ktlint.core.ast.nextLeaf
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.kdoc.psi.api.KDoc

class KDocLeadingAsteriskRule : Rule("kdoc-leading-asterisk") {
  override fun beforeVisitChildNodes(
    node: ASTNode,
    autoCorrect: Boolean,
    emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
  ) {

    if (node.elementType == ElementType.KDOC_START) {
      visitKDoc(node, autoCorrect = autoCorrect, emit = emit)
    }
  }

  private fun visitKDoc(
    kdocNode: ASTNode,
    autoCorrect: Boolean,
    emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
  ) {

    val kdoc = kdocNode.psi.parent as KDoc

    val indent = kdoc.findIndent()
    val newlineIndent = "\n$indent"

    kdoc.node.depthFirst()
      .distinct()
      .filter { it.elementType == WHITE_SPACE }
      .filter { it.text.lines().size > 1 }
      .filter { !it.isWhitespaceBeforeKDocLeadingAsterisk() }
      .filter { node ->
        val next = node.nextLeaf()

        when {
          next == null -> false
          next.isWhiteSpace() -> false
          next.isKDocLeadingAsterisk() -> false
          next.isKDocEnd() && node.text.lines().count() <= 2 -> false
          else -> true
        }
      }
      .toList()
      .forEach { node ->

        val parent = node.parent!!

        val next = node.nextSibling() ?: return@forEach

        emit(next.startOffset, "kdoc leading asterisk", true)

        if (autoCorrect) {

          val numLines = node.text.split("\n").size - 1

          parent.removeChild(node)

          repeat(numLines) { i ->
            if (i == numLines - 1 && next.elementType == KDOC_END) {
              parent.addChild(PsiWhiteSpaceImpl(newlineIndent.dropLast(1)), next)
            } else {
              parent.addChild(PsiWhiteSpaceImpl(newlineIndent), next)
              parent.addChild(LeafPsiElement(ElementType.KDOC_LEADING_ASTERISK, "*"), next)
            }
          }

          if (!next.isWhiteSpaceWithNewline()) {
            parent.addChild(PsiWhiteSpaceImpl(" "), next)
          }
        }
      }
  }
}
