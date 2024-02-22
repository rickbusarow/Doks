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

package com.rickbusarow.doks.internal.stdlib

import java.io.File
import java.nio.file.Path
import kotlin.LazyThreadSafetyMode.NONE

private val kotlinExtensions: Set<String> by lazy(NONE) { setOf("kt", "kts") }

/**
 * `isFile && extension in setOf("kt", "kts")`
 *
 * @since 0.2.0
 */
internal fun File.isExistanttKotlinFile(): Boolean {
  return isFile && extension in kotlinExtensions
}

/**
 * `extension in setOf("kt", "kts")`
 *
 * @since 0.2.0
 */
internal fun File.isKotlinFile(): Boolean {
  return extension in kotlinExtensions
}

/**
 * Walks up the tree until [parentFile][File.getParentFile] is null. The first
 * element is the immediate parent of the receiver, and the last is the root.
 *
 * @since 0.1.0
 */
internal fun File.parents(): Sequence<File> = generateSequence(this) { it.parentFile }

internal fun File.segments(): List<String> = path.split(File.separator)

/**
 * Makes parent directories, then creates the receiver file. If a
 * [content] argument was provided, it will be written to the newly-created
 * file. If the file already existed, its content will be overwritten.
 *
 * @see Path.createSafely
 * @since 0.1.0
 */
internal fun File.createSafely(content: String? = null): File = apply {
  if (content != null) {
    makeParentDir().writeText(content)
  } else {
    makeParentDir().createNewFile()
  }
}

/**
 * Makes parent directories, then creates the receiver file. If a
 * [content] argument was provided, it will be written to the newly-created
 * file. If the file already existed, its content will be overwritten.
 *
 * @see File.createSafely
 * @since 0.1.1
 */
internal fun Path.createSafely(content: String? = null): File = toFile().createSafely(content)

/**
 * Creates the directories if they don't already exist.
 *
 * @see File.mkdirs
 * @see File.makeParentDir
 * @see Path.mkdirsInline
 * @since 0.1.0
 */
internal fun File.mkdirsInline(): File = apply(File::mkdirs)

/**
 * Creates the directories if they don't already exist.
 *
 * @see File.mkdirs
 * @see File.makeParentDir
 * @see File.mkdirsInline
 * @since 0.1.1
 */
internal fun Path.mkdirsInline(): Path = apply { toFile().mkdirsInline() }

/**
 * Creates the parent directory if it doesn't already exist.
 *
 * @see File.mkdirsInline
 * @see File.mkdirs
 * @see Path.makeParentDir
 * @since 0.1.0
 */
internal fun File.makeParentDir(): File = apply {
  val fileParent = parentFile.requireNotNull { "File's `parentFile` must not be null." }
  fileParent.mkdirs()
}

/**
 * Creates the parent directory if it doesn't already exist.
 *
 * @see File.mkdirsInline
 * @see File.mkdirs
 * @see File.makeParentDir
 * @since 0.1.1
 */
internal fun Path.makeParentDir(): Path = apply {
  val fileParent = parent.requireNotNull { "Path's `parentFile` must not be null." }
  fileParent.mkdirsInline()
}
