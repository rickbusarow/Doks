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

import com.rickbusarow.kase.KaseMatrix
import com.rickbusarow.kase.gradle.DslLanguage
import com.rickbusarow.kase.gradle.GradleKotlinTestVersions
import com.rickbusarow.kase.gradle.KaseGradleTest
import com.rickbusarow.kase.gradle.versions
import org.junit.jupiter.api.DynamicNode
import java.util.stream.Stream

internal abstract class DoksGradleTest(
  override val kaseMatrix: KaseMatrix = DoksVersionMatrix()
) : KaseGradleTest<DoksGradleTestParams, DoksGradleTestEnvironment, DoksGradleTestEnvironment.Factory>,
  MoreAsserts {

  override val testEnvironmentFactory = DoksGradleTestEnvironment.Factory()

  override val params: List<DoksGradleTestParams>
    get() = params(DslLanguage.KotlinDsl(useInfix = true, useLabels = false))

  fun params(dslLanguage: DslLanguage): List<DefaultDoksGradleTestParams> {
    return kaseMatrix.versions(GradleKotlinTestVersions)
      .map { (gradle, kotlin) ->
        DefaultDoksGradleTestParams(
          dslLanguage = dslLanguage,
          gradle = gradle,
          kotlin = kotlin
        )
      }
  }

  fun testFactory(
    dslLanguage: DslLanguage = DslLanguage.KotlinDsl(useInfix = true, useLabels = false),
    testAction: suspend DoksGradleTestEnvironment.(DoksGradleTestParams) -> Unit
  ): Stream<out DynamicNode> = params(dslLanguage).asTests(testAction = testAction)
}
