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

import com.rickbusarow.docusync.markdown.markdown
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import java.io.File

/** @since 0.1.0 */
internal class DocusyncEngine(
  private val ruleCache: Rules,
  private val autoCorrect: Boolean
) : java.io.Serializable {

  /** @since 0.1.0 */
  fun run(files: List<File>): List<FileResult> = runBlocking {

    files.map { file ->
      async(Dispatchers.Default) {

        file.markdown(
          rules = ruleCache,
          autoCorrect = autoCorrect
        )
      }
    }
      .awaitAll()
  }

  /** @since 0.1.0 */
  fun run(file: File): FileResult {
    return file.markdown(
      rules = ruleCache,
      autoCorrect = autoCorrect
    )
  }

  /**
   * @property file the targeted file
   * @property changed true if [oldText] and [newText] are different
   * @property oldText the original contents of the file
   * @property newText the new contents of the file
   * @since 0.1.0
   */
  data class FileResult(
    val file: File,
    val changed: Boolean,
    val oldText: String,
    val newText: String
  ) : java.io.Serializable
}
