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

package com.rickbusarow.docusync.markdown

import com.rickbusarow.docusync.DocusyncEngine.FileResult
import com.rickbusarow.docusync.Rules
import com.rickbusarow.docusync.internal.stdlib.joinToStringConcat
import java.io.File

internal const val OPEN = "<!--docusync"
internal const val CLOSE = "<!--/docusync-->"
internal val openReg = Regex("""($OPEN\s)([\s\S]*?)(-->)""")
internal val closeReg = """([\s\S]*?)(<!--\/docusync\s*?-->)([\s\S]*)""".toRegex()

internal fun String.markdown(
  absolutePath: String,
  rules: Rules,
  autoCorrect: Boolean
): String {

  val fullText = this

  val (beforeFirstNodes, opened) = MarkdownNode.from(fullText)
    .depthFirst()
    .filter { it.isLeaf }
    .toList()
    .split { it.isOpeningTag() }
    .partition { it.firstOrNull()?.isOpeningTag() == false }

  val beforeFirst = beforeFirstNodes.singleOrNull()?.concat().orEmpty()

  val sections = opened.map { it.toMarkdownSection() }

  sections.forEachIndexed { index, section ->

    if (index != sections.lastIndex) {
      checkNotNull(section.closeTag) {

        val leading = beforeFirst + sections.take(index).joinToStringConcat { it.match }

        val position = section.position(leading, section.openTagFull)

        "Docusync - file://$absolutePath:${position.row}:${position.column} > " +
          "The tag '${section.openTagFull}' must be closed with " +
          "`$CLOSE` before the next docusync opening tag."
      }
    }
  }

  if (sections.isEmpty()) {
    return this@markdown
  }

  val replacedFullString = beforeFirst + sections.joinToStringConcat { section ->

    val newBody = section.ruleConfigs
      .fold(section.body) { acc, ruleConfig ->

        val rule = rules[ruleConfig.name]

        val matches = rule.regex.findAll(acc).toList()

        ruleConfig.checkCount(matches.map { it.value })

        rule.replaceIn(acc)
      }

    with(section) {
      "$openTagFull$newBody${closeTag.orEmpty()}$afterCloseTag"
    }
  }

  if (this != replacedFullString) {
    check(autoCorrect) { "Docusync - file://$absolutePath > text is out of date" }
  }

  return replacedFullString
}

private fun List<MarkdownNode>.concat() = joinToStringConcat { it.text }

@Suppress("MagicNumber")
private fun List<MarkdownNode>.toMarkdownSection(): MarkdownSection {

  val openTagFull = first().text

  val groupValues = openReg.find(openTagFull)!!.groupValues

  val openTagStart = groupValues[1]
  val openTagMatchersBlob = groupValues[2]
  val openTagEnd = groupValues[3]

  val body = drop(1)
    .takeWhile { !it.isClosingTag() }.concat()

  val closeTag = firstOrNull { it.isClosingTag() }?.text

  val afterCloseTag = dropWhile { !it.isClosingTag() }
    .drop(1)
    .concat()

  return MarkdownSection(
    match = concat(),
    openTagFull = openTagFull,
    openTagStart = openTagStart,
    openTagMatchersBlob = openTagMatchersBlob,
    openTagEnd = openTagEnd,
    body = body,
    closeTag = closeTag,
    afterCloseTag = afterCloseTag
  )
}

internal fun File.markdown(
  rules: Rules,
  autoCorrect: Boolean
): FileResult {

  require(extension == "md" || extension == "mdx") {
    "This file doesn't seem to be markdown: file://$absolutePath"
  }

  val old = readText()

  val new = old.markdown(
    absolutePath = absolutePath,
    rules = rules,
    autoCorrect = autoCorrect
  )

  val changed = old != new
  if (changed) {
    writeText(new)
    println("wrote changes to file://$path")
  }
  return FileResult(
    file = this,
    changed = changed,
    oldText = old,
    newText = new
  )
}

/**
 * Splits the elements by [predicate], where the element matching [predicate] is the first element of
 * each nested list. If the original list starts with an element which does not match [predicate], then
 * the first nested list will contain all elements before the first matching element.
 */
internal fun <E> List<E>.split(predicate: (E) -> Boolean): List<List<E>> {

  val source = toMutableList()

  return buildList {
    while (true) {
      val element = source.removeFirstOrNull() ?: break
      add(
        buildList {
          add(element)
          val notPredicate = source.takeWhile { !predicate(it) }
          addAll(notPredicate)
          repeat(notPredicate.size) { source.removeFirst() }
        }
      )
    }
  }
}
