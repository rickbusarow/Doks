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

import com.rickbusarow.docusync.MarkdownGroup.MarkdownCloseTag
import java.io.File

internal fun String.markdown(
  absolutePath: String,
  replacers: Map<String, Replacer>,
  autoCorrect: Boolean
): String {

  val toParse = this

  val openReg =
    """([\s\S]*?)((<!---docusync\s)([\s\S]*?)(-->))([\s\S]*?(?:(?=<!---docusync[\s\S]*?-->)|\z))""".toRegex()

  val closeReg = """([\s\S]*?)(!---\/docusync\s*?-->)([\s\S]*)""".toRegex()

  @Suppress("MagicNumber")
  val groups = openReg.findAll(toParse)
    .toList()
    .map { matchResult ->

      matchResult.toMarkdownGroup(closeReg)
    }

  groups.forEachIndexed { index, group ->

    if (index != groups.lastIndex) {
      checkNotNull(group.closeTag) {

        val position = group.position(groups.take(index), group.openTagFull)

        "Docusync - file://$absolutePath:${position.row}:${position.column} > " +
          "The tag '${group.openTagFull}' must be closed with " +
          "`<---/docusync-->` before the next docusync opening tag."
      }
    }
  }

  if (groups.isEmpty()) {
    return this@markdown
  }

  val replacedFullString = groups.joinToString("") { group ->

    val newBody = group.replacerConfigs
      .fold(group.body) { acc, replacerConfig ->

        val id = replacerConfig.id

        val replacer = replacers[id]
          ?: error("There is no defined replacer for the id of '$id'")

        val matches = replacer.regex.findAll(acc).toList()

        replacerConfig.checkCount(matches.map { it.value })

        acc.replace(replacer.regex, replacer.replacement)
      }

    with(group) {
      "$beforeOpenTag$openTagFull$newBody${closeTag?.tag.orEmpty()}${closeTag?.remainder.orEmpty()}"
    }
  }

  if (this != replacedFullString) {
    check(autoCorrect) { "Docusync - file://$absolutePath > text is out of date" }
  }

  return replacedFullString
}

@Suppress("MagicNumber")
private fun MatchResult.toMarkdownGroup(closeReg: Regex): MarkdownGroup {
  val beforeOpenTag = groupValues[1]
  val openTagFull = groupValues[2]
  val openTagStart = groupValues[3]
  val openTagMatchersBlob = groupValues[4]
  val openTagEnd = groupValues[5]
  val afterOpenBlob = groupValues[6]

  var body = afterOpenBlob

  val closeOrNull = closeReg.find(afterOpenBlob)
    ?.let { mr ->
      body = mr.groupValues[1]
      MarkdownCloseTag(
        tag = mr.groupValues[2],
        remainder = mr.groupValues[3]
      )
    }

  return MarkdownGroup(
    match = value,
    beforeOpenTag = beforeOpenTag,
    openTagFull = openTagFull,
    openTagStart = openTagStart,
    openTagMatchersBlob = openTagMatchersBlob,
    openTagEnd = openTagEnd,
    body = body,
    closeTag = closeOrNull
  )
}

internal fun File.markdown(
  replacers: Map<String, Replacer>,
  autoCorrect: Boolean
): Boolean {

  require(extension == "md" || extension == "mdx") {
    "This file doesn't seem to be markdown: file://$absolutePath"
  }

  val old = readText()

  val new = old.markdown(
    absolutePath = absolutePath,
    replacers = replacers,
    autoCorrect = autoCorrect
  )

  val changed = old != new
  if (changed) {

    // TODO <Rick> delete me
    println("<Rick> 128 -- write new text to -- file://$absolutePath")

    writeText(new)
  }
  return changed
}
