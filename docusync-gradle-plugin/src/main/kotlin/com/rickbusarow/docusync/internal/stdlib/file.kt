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

package com.rickbusarow.docusync.internal.stdlib

import java.io.File

/**
 * Walks up the tree until [parentFile][File.getParentFile] is null. The first element is the immediate
 * parent of the receiver, and the last is the root.
 */
internal fun File.parents(): Sequence<File> = generateSequence(this) { it.parentFile }

/**
 * Makes parent directories, then creates the receiver file. If a [content] argument was provided, it
 * will be written to the newly-created file. If the file already existed, its content will be
 * overwritten.
 */
internal fun File.createSafely(content: String? = null): File = apply {
  if (content != null) {
    makeParentDir().writeText(content)
  } else {
    makeParentDir().createNewFile()
  }
}

/**
 * Creates the directories if they don't already exist.
 *
 * @see File.mkdirs
 * @see File.makeParentDir
 */
internal fun File.mkdirsInline(): File = apply(File::mkdirs)

/**
 * Creates the parent directory if it doesn't already exist.
 *
 * @see File.mkdirsInline
 * @see File.mkdirs
 */
internal fun File.makeParentDir(): File = apply {
  val fileParent = parentFile.requireNotNull {
    "File's `parentFile` must not be null."
  }
  fileParent.mkdirs()
}
