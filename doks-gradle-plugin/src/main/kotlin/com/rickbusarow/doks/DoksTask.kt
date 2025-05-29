/*
 * Copyright (C) 2025 Rick Busarow
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

package com.rickbusarow.doks

import org.gradle.api.DefaultTask

/**
 * The base class for all Doks tasks.
 *
 * @since 0.1.0
 */
@Suppress("UnnecessaryAbstractClass")
public abstract class DoksTask(description: String) : DefaultTask() {
  init {
    group = "Doks"
    this.description = description
  }
}
