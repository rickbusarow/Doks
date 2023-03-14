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

import com.charleskorn.kaml.SingleLineStringStyle.Plain
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.rickbusarow.docusync.internal.existsOrNull
import com.rickbusarow.docusync.markdown.markdown
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/** */
class DocusyncEngine(
  private val ruleCache: RuleCache,
  private val autoCorrect: Boolean
) : java.io.Serializable {

  /** */
  fun run(files: List<File>): Boolean = runBlocking {

    files.map { file ->
      async(Dispatchers.Default) {

        file.markdown(
          rules = ruleCache.get(file),
          autoCorrect = autoCorrect
        )
      }
    }
      .awaitAll()
      .all { it }
  }

  /** */
  fun run(file: File): Boolean {
    return file.markdown(
      rules = ruleCache.get(file),
      autoCorrect = autoCorrect
    )
  }
}

/** */
class RuleCache(
  globalRules: List<Rule>
) : java.io.Serializable {

  private val globalRulesMap = globalRules.associateBy { it.name }

  @delegate:Transient
  private val yaml: Yaml by lazy {
    Yaml(configuration = YamlConfiguration(encodingIndentationSize = 2, singleLineStringStyle = Plain))
  }

  private val cache = ConcurrentHashMap<File, Lazy<Map<String, Rule>>>()

  /**
   * Parses the file tree for all [Rule]s defined in this directory and all parent directories.
   */
  fun get(file: File): Map<String, Rule> {

    return if (file.isFile) {
      file.parentFile?.let { get(it) }.orEmpty()
    } else {
      cache.computeIfAbsent(file) {
        lazy {
          val here = file.resolve("docusync.yml")
            .existsOrNull()
            ?.readText()
            ?.let { yaml.decodeFromString<List<Rule>>(it) }
            .orEmpty()
            .associateBy { it.name }

          val parentRules = file.parentFile?.let { get(it) }
            ?: globalRulesMap

          parentRules + here
        }
      }.value
    }
  }
}
