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

package com.rickbusarow.doks

import com.rickbusarow.kase.AbstractKase3
import com.rickbusarow.kase.Kase3
import com.rickbusarow.kase.gradle.DslLanguage
import com.rickbusarow.kase.gradle.GradleDependencyVersion
import com.rickbusarow.kase.gradle.GradleKotlinTestVersions
import com.rickbusarow.kase.gradle.HasDslLanguage
import com.rickbusarow.kase.gradle.KotlinDependencyVersion
import kotlin.LazyThreadSafetyMode.NONE

internal interface DoksGradleTestParams :
  Kase3<GradleDependencyVersion, KotlinDependencyVersion, DslLanguage>,
  HasDslLanguage,
  GradleKotlinTestVersions

internal class DefaultDoksGradleTestParams(
  override val dslLanguage: DslLanguage,
  override val gradle: GradleDependencyVersion,
  override val kotlin: KotlinDependencyVersion
) : AbstractKase3<GradleDependencyVersion, KotlinDependencyVersion, DslLanguage>(
  a1 = gradle,
  a2 = kotlin,
  a3 = dslLanguage
),
  DoksGradleTestParams {

  override val gradleVersion: String get() = gradle.value
  override val kotlinVersion: String get() = kotlin.value

  override val displayName: String by lazy(NONE) {
    "dsl: ${dslLanguage::class.simpleName} | gradle: $gradle | kotlin: $kotlin"
  }
}
