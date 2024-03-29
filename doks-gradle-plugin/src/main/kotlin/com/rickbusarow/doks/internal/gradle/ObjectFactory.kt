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

package com.rickbusarow.doks.internal.gradle

import org.gradle.api.Action
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.model.ObjectFactory

internal fun ObjectFactory.fileTree(baseDir: Any): ConfigurableFileTree = fileTree().from(baseDir)

internal fun ObjectFactory.fileTree(
  baseDir: Any,
  configureAction: Action<in ConfigurableFileTree>
): ConfigurableFileTree {
  return fileTree().from(baseDir).also(configureAction::execute)
}
