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

package com.rickbusarow.doks.internal.sharding

import com.rickbusarow.doks.internal.sharding.Shard.Companion.mermaid
import com.rickbusarow.doks.internal.sharding.Shard.Companion.toShards
import com.rickbusarow.doks.internal.sharding.Shard.Companion.workflow
import com.rickbusarow.doks.internal.sharding.TaskNode.Companion.mermaid
import com.rickbusarow.doks.internal.sharding.TaskNode.Companion.toNodes
import com.rickbusarow.doks.internal.sharding.TaskWithDependencyNames.Companion.mermaid
import com.rickbusarow.doks.internal.sharding.TaskWithDependencyNames.Companion.removeRedundantDependencies
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.io.File

operator fun File.div(relativePath: String): File = resolve(relativePath)

class ShardTests {

  val nodes1 = listOf(
    TaskWithDependencyNames(
      ":check",
      setOf(":hermit-core:samples:lintKotlinTest", ":hermit-coroutines:processResources", ":hermit-junit5:samples:processTestResources", ":hermit-coroutines:test", ":hermit-junit4:samples:compileJava", ":hermit-junit5:lintKotlin", ":hermit-junit4:jar", ":hermit-mockk:samples:compileKotlin", ":hermit-core:samples:compileKotlin", ":hermit-junit5:testClasses", ":hermit-junit4:compileTestKotlin", ":hermit-junit5:samples:compileKotlin", ":hermit-mockk:apiCheck", ":hermit-junit4:lintKotlinTest", ":hermit-junit5:processResources", ":hermit-mockk:compileTestJava", ":hermit-core:test", ":hermit-mockk:compileTestKotlin", ":hermit-core:lintKotlinMain", ":hermit-mockk:samples:classes", ":hermit-junit4:samples:test", ":hermit-core:samples:processResources", ":hermit-junit5:jar", ":hermit-junit5:samples:lintKotlinTest", ":hermit-junit4:compileKotlin", ":hermit-junit4:samples:classes", ":hermit-junit4:compileJava", ":hermit-mockk:compileJava", ":hermit-mockk:samples:compileTestJava", ":hermit-core:lintKotlin", ":hermit-junit5:samples:testClasses", ":hermit-coroutines:samples:compileJava", ":apiCheck", ":hermit-core:compileTestJava", ":test", ":hermit-junit5:samples:compileTestKotlin", ":hermit-coroutines:samples:testClasses", ":hermit-coroutines:samples:classes", ":hermit-junit5:apiCheck", ":hermit-core:samples:compileTestJava", ":hermit-junit5:apiBuild", ":hermit-core:samples:classes", ":hermit-mockk:samples:processTestResources", ":hermit-coroutines:apiCheck", ":hermit-coroutines:samples:compileTestKotlin", ":hermit-junit4:lintKotlin", ":hermit-mockk:samples:testClasses", ":hermit-junit4:processResources", ":hermit-junit4:classes", ":hermit-mockk:samples:lintKotlinMain", ":hermit-junit5:processTestResources", ":hermit-coroutines:compileTestJava", ":hermit-junit5:classes", ":hermit-junit4:apiBuild", ":hermit-coroutines:lintKotlinMain", ":hermit-mockk:samples:compileTestKotlin", ":hermit-core:apiBuild", ":hermit-core:compileTestKotlin", ":hermit-core:samples:test", ":hermit-junit4:testClasses", ":hermit-junit4:compileTestJava", ":hermit-core:processResources", ":hermit-coroutines:compileTestKotlin", ":hermit-junit4:samples:processResources", ":hermit-coroutines:samples:lintKotlinMain", ":hermit-mockk:processResources", ":hermit-junit5:compileTestKotlin", ":hermit-coroutines:samples:processResources", ":hermit-junit4:samples:compileTestKotlin", ":hermit-coroutines:samples:compileKotlin", ":hermit-junit4:samples:compileKotlin", ":hermit-junit4:samples:lintKotlinTest", ":hermit-junit5:samples:test", ":hermit-core:samples:compileTestKotlin", ":hermit-junit5:samples:processResources", ":hermit-mockk:jar", ":hermit-junit4:test", ":hermit-coroutines:samples:compileTestJava", ":hermit-coroutines:classes", ":hermit-core:samples:testClasses", ":hermit-junit5:transformAtomicfuClasses", ":hermit-core:compileKotlin", ":hermit-mockk:testClasses", ":hermit-core:classes", ":hermit-core:processTestResources", ":hermit-junit5:test", ":hermit-coroutines:dependencyGuard", ":hermit-coroutines:lintKotlinTest", ":hermit-mockk:processTestResources", ":hermit-junit5:compileKotlin", ":hermit-mockk:lintKotlin", ":hermit-core:compileJava", ":hermit-junit5:dependencyGuard", ":hermit-coroutines:samples:processTestResources", ":hermit-junit4:samples:lintKotlinMain", ":hermit-junit5:lintKotlinTest", ":hermit-core:dependencyGuard", ":hermit-core:lintKotlinTest", ":hermit-junit4:apiCheck", ":hermit-junit5:samples:lintKotlin", ":hermit-core:apiCheck", ":hermit-core:testClasses", ":hermit-core:samples:processTestResources", ":hermit-junit5:samples:compileTestJava", ":hermit-mockk:samples:processResources", ":hermit-junit5:samples:lintKotlinMain", ":detekt", ":moduleCheck", ":hermit-junit4:samples:testClasses", ":hermit-coroutines:lintKotlin", ":hermit-junit5:lintKotlinMain", ":hermit-junit4:dependencyGuard", ":hermit-junit5:samples:compileJava", ":hermit-core:samples:lintKotlin", ":hermit-junit5:transformTestAtomicfuClasses", ":hermit-mockk:classes", ":lintKotlin", ":hermit-mockk:dependencyGuard", ":hermit-coroutines:samples:test", ":hermit-junit4:samples:lintKotlin", ":hermit-coroutines:processTestResources", ":hermit-coroutines:jar", ":hermit-junit5:compileTestJava", ":hermit-coroutines:samples:lintKotlin", ":hermit-mockk:samples:lintKotlin", ":hermit-coroutines:testClasses", ":hermit-coroutines:compileKotlin", ":hermit-core:samples:lintKotlinMain", ":hermit-coroutines:apiBuild", ":hermit-junit5:compileJava", ":hermit-mockk:compileKotlin", ":hermit-junit4:lintKotlinMain", ":hermit-mockk:samples:test", ":hermit-mockk:samples:lintKotlinTest", ":hermit-coroutines:samples:lintKotlinTest", ":hermit-mockk:samples:compileJava", ":hermit-core:samples:compileJava", ":hermit-coroutines:compileJava", ":hermit-mockk:lintKotlinTest", ":hermit-junit4:samples:compileTestJava", ":hermit-junit4:samples:processTestResources", ":hermit-junit5:samples:classes", ":hermit-junit4:processTestResources", ":hermit-mockk:test", ":hermit-mockk:apiBuild", ":hermit-core:jar", ":hermit-mockk:lintKotlinMain")
    ),
    TaskWithDependencyNames(
      ":hermit-core:check",
      setOf(
        ":hermit-core:dependencyGuard",
        ":hermit-core:apiCheck",
        ":hermit-core:test",
        ":hermit-core:lintKotlin"
      )
    ),
    TaskWithDependencyNames(
      ":hermit-coroutines:check",
      setOf(
        ":hermit-coroutines:dependencyGuard",
        ":hermit-coroutines:apiCheck",
        ":hermit-coroutines:test",
        ":hermit-coroutines:lintKotlin"
      )
    ),
    TaskWithDependencyNames(
      ":hermit-junit4:check",
      setOf(
        ":hermit-junit4:test",
        ":hermit-junit4:lintKotlin",
        ":hermit-junit4:apiCheck",
        ":hermit-junit4:dependencyGuard"
      )
    ),
    TaskWithDependencyNames(
      ":hermit-junit5:check",
      setOf(
        ":hermit-junit5:dependencyGuard",
        ":hermit-junit5:test",
        ":hermit-junit5:lintKotlin",
        ":hermit-junit5:apiCheck"
      )
    ),
    TaskWithDependencyNames(
      ":hermit-mockk:check",
      setOf(
        ":hermit-mockk:lintKotlin",
        ":hermit-mockk:dependencyGuard",
        ":hermit-mockk:test",
        ":hermit-mockk:apiCheck"
      )
    ),
    TaskWithDependencyNames(
      ":hermit-core:samples:check",
      setOf(":hermit-core:samples:test", ":hermit-core:samples:lintKotlin")
    ),
    TaskWithDependencyNames(
      ":hermit-coroutines:samples:check",
      setOf(":hermit-coroutines:samples:lintKotlin", ":hermit-coroutines:samples:test")
    ),
    TaskWithDependencyNames(
      ":hermit-junit4:samples:check",
      setOf(":hermit-junit4:samples:test", ":hermit-junit4:samples:lintKotlin")
    ),
    TaskWithDependencyNames(
      ":hermit-junit5:samples:check",
      setOf(":hermit-junit5:samples:lintKotlin", ":hermit-junit5:samples:test")
    ),
    TaskWithDependencyNames(
      ":hermit-mockk:samples:check",
      setOf(":hermit-mockk:samples:test", ":hermit-mockk:samples:lintKotlin")
    ),
    TaskWithDependencyNames(
      ":test",
      setOf(
        ":compileTestKotlin",
        ":compileTestJava",
        ":testClasses",
        ":classes",
        ":compileJava",
        ":compileKotlin"
      )
    ),
    TaskWithDependencyNames(":lintKotlin", setOf(":lintKotlinTest", ":lintKotlinMain")),
    TaskWithDependencyNames(":detekt", setOf()),
    TaskWithDependencyNames(":moduleCheck", setOf()),
    TaskWithDependencyNames(":apiCheck", setOf(":apiBuild")),
    TaskWithDependencyNames(":hermit-core:dependencyGuard", setOf()),
    TaskWithDependencyNames(":hermit-core:apiCheck", setOf(":hermit-core:apiBuild")),
    TaskWithDependencyNames(
      ":hermit-core:test",
      setOf(
        ":hermit-core:compileTestJava",
        ":hermit-core:compileTestKotlin",
        ":hermit-core:testClasses",
        ":hermit-core:classes",
        ":hermit-core:compileKotlin",
        ":hermit-core:compileJava"
      )
    ),
    TaskWithDependencyNames(
      ":hermit-core:lintKotlin",
      setOf(":hermit-core:lintKotlinMain", ":hermit-core:lintKotlinTest")
    ),
    TaskWithDependencyNames(":hermit-coroutines:dependencyGuard", setOf()),
    TaskWithDependencyNames(":hermit-coroutines:apiCheck", setOf(":hermit-coroutines:apiBuild")),
    TaskWithDependencyNames(
      ":hermit-coroutines:test",
      setOf(
        ":hermit-coroutines:compileTestJava",
        ":hermit-coroutines:compileTestKotlin",
        ":hermit-coroutines:testClasses",
        ":hermit-coroutines:classes",
        ":hermit-coroutines:compileKotlin",
        ":hermit-coroutines:compileJava",
        ":hermit-junit5:jar",
        ":hermit-core:jar"
      )
    ),
    TaskWithDependencyNames(
      ":hermit-coroutines:lintKotlin",
      setOf(":hermit-coroutines:lintKotlinTest", ":hermit-coroutines:lintKotlinMain")
    ),
    TaskWithDependencyNames(
      ":hermit-junit4:test",
      setOf(
        ":hermit-junit4:compileTestJava",
        ":hermit-junit4:compileTestKotlin",
        ":hermit-junit4:testClasses",
        ":hermit-junit4:classes",
        ":hermit-junit4:compileKotlin",
        ":hermit-junit4:compileJava",
        ":hermit-core:jar"
      )
    ),
    TaskWithDependencyNames(
      ":hermit-junit4:lintKotlin",
      setOf(":hermit-junit4:lintKotlinTest", ":hermit-junit4:lintKotlinMain")
    ),
    TaskWithDependencyNames(":hermit-junit4:apiCheck", setOf(":hermit-junit4:apiBuild")),
    TaskWithDependencyNames(":hermit-junit4:dependencyGuard", setOf()),
    TaskWithDependencyNames(":hermit-junit5:dependencyGuard", setOf()),
    TaskWithDependencyNames(
      ":hermit-junit5:test",
      setOf(":hermit-junit5:compileTestJava", ":hermit-junit5:compileTestKotlin", ":hermit-junit5:testClasses", ":hermit-junit5:transformTestAtomicfuClasses", ":hermit-junit5:classes", ":hermit-junit5:transformAtomicfuClasses", ":hermit-junit5:compileKotlin", ":hermit-junit5:compileJava", ":hermit-core:jar")
    ),
    TaskWithDependencyNames(
      ":hermit-junit5:lintKotlin",
      setOf(":hermit-junit5:lintKotlinTest", ":hermit-junit5:lintKotlinMain")
    ),
    TaskWithDependencyNames(":hermit-junit5:apiCheck", setOf(":hermit-junit5:apiBuild")),
    TaskWithDependencyNames(
      ":hermit-mockk:lintKotlin",
      setOf(":hermit-mockk:lintKotlinTest", ":hermit-mockk:lintKotlinMain")
    ),
    TaskWithDependencyNames(":hermit-mockk:dependencyGuard", setOf()),
    TaskWithDependencyNames(
      ":hermit-mockk:test",
      setOf(
        ":hermit-mockk:compileTestJava",
        ":hermit-mockk:compileTestKotlin",
        ":hermit-mockk:testClasses",
        ":hermit-mockk:classes",
        ":hermit-mockk:compileKotlin",
        ":hermit-mockk:compileJava",
        ":hermit-core:jar"
      )
    ),
    TaskWithDependencyNames(":hermit-mockk:apiCheck", setOf(":hermit-mockk:apiBuild")),
    TaskWithDependencyNames(
      ":hermit-core:samples:test",
      setOf(
        ":hermit-core:samples:compileTestJava",
        ":hermit-core:samples:compileTestKotlin",
        ":hermit-core:samples:testClasses",
        ":hermit-core:samples:classes",
        ":hermit-core:samples:compileKotlin",
        ":hermit-core:samples:compileJava",
        ":hermit-core:jar"
      )
    ),
    TaskWithDependencyNames(
      ":hermit-core:samples:lintKotlin",
      setOf(":hermit-core:samples:lintKotlinTest", ":hermit-core:samples:lintKotlinMain")
    ),
    TaskWithDependencyNames(
      ":hermit-coroutines:samples:lintKotlin",
      setOf(":hermit-coroutines:samples:lintKotlinTest", ":hermit-coroutines:samples:lintKotlinMain")
    ),
    TaskWithDependencyNames(
      ":hermit-coroutines:samples:test",
      setOf(":hermit-coroutines:samples:compileTestKotlin", ":hermit-coroutines:samples:compileTestJava", ":hermit-coroutines:samples:testClasses", ":hermit-coroutines:samples:classes", ":hermit-coroutines:samples:compileKotlin", ":hermit-coroutines:samples:compileJava", ":hermit-coroutines:jar", ":hermit-junit5:jar", ":hermit-core:jar")
    ),
    TaskWithDependencyNames(
      ":hermit-junit4:samples:test",
      setOf(
        ":hermit-junit4:samples:compileTestJava",
        ":hermit-junit4:samples:compileTestKotlin",
        ":hermit-junit4:samples:testClasses",
        ":hermit-junit4:samples:classes",
        ":hermit-junit4:samples:compileJava",
        ":hermit-junit4:samples:compileKotlin",
        ":hermit-junit4:jar",
        ":hermit-core:jar"
      )
    ),
    TaskWithDependencyNames(
      ":hermit-junit4:samples:lintKotlin",
      setOf(":hermit-junit4:samples:lintKotlinTest", ":hermit-junit4:samples:lintKotlinMain")
    ),
    TaskWithDependencyNames(
      ":hermit-junit5:samples:lintKotlin",
      setOf(":hermit-junit5:samples:lintKotlinMain", ":hermit-junit5:samples:lintKotlinTest")
    ),
    TaskWithDependencyNames(
      ":hermit-junit5:samples:test",
      setOf(
        ":hermit-junit5:samples:compileTestKotlin",
        ":hermit-junit5:samples:compileTestJava",
        ":hermit-junit5:samples:testClasses",
        ":hermit-junit5:samples:classes",
        ":hermit-junit5:samples:compileJava",
        ":hermit-junit5:samples:compileKotlin",
        ":hermit-junit5:jar",
        ":hermit-core:jar"
      )
    ),
    TaskWithDependencyNames(
      ":hermit-mockk:samples:test",
      setOf(":hermit-mockk:samples:compileTestKotlin", ":hermit-mockk:samples:compileTestJava", ":hermit-mockk:samples:testClasses", ":hermit-mockk:samples:classes", ":hermit-mockk:samples:compileKotlin", ":hermit-mockk:samples:compileJava", ":hermit-junit5:jar", ":hermit-mockk:jar", ":hermit-core:jar")
    ),
    TaskWithDependencyNames(
      ":hermit-mockk:samples:lintKotlin",
      setOf(":hermit-mockk:samples:lintKotlinTest", ":hermit-mockk:samples:lintKotlinMain")
    ),
    TaskWithDependencyNames(":compileTestKotlin", setOf(":classes", ":compileJava", ":compileKotlin")),
    TaskWithDependencyNames(
      ":compileTestJava",
      setOf(":classes", ":compileJava", ":compileKotlin", ":compileTestKotlin")
    ),
    TaskWithDependencyNames(":testClasses", setOf(":processTestResources", ":compileTestJava")),
    TaskWithDependencyNames(":classes", setOf(":compileJava", ":processResources")),
    TaskWithDependencyNames(":compileJava", setOf(":compileKotlin")),
    TaskWithDependencyNames(":compileKotlin", setOf()),
    TaskWithDependencyNames(":lintKotlinTest", setOf()),
    TaskWithDependencyNames(":lintKotlinMain", setOf()),
    TaskWithDependencyNames(":apiBuild", setOf(":compileJava", ":compileKotlin")),
    TaskWithDependencyNames(
      ":hermit-core:apiBuild",
      setOf(":hermit-core:compileKotlin", ":hermit-core:compileJava")
    ),
    TaskWithDependencyNames(
      ":hermit-core:compileTestJava",
      setOf(
        ":hermit-core:classes",
        ":hermit-core:compileKotlin",
        ":hermit-core:compileJava",
        ":hermit-core:compileTestKotlin"
      )
    ),
    TaskWithDependencyNames(
      ":hermit-core:compileTestKotlin",
      setOf(":hermit-core:classes", ":hermit-core:compileKotlin", ":hermit-core:compileJava")
    ),
    TaskWithDependencyNames(
      ":hermit-core:testClasses",
      setOf(":hermit-core:processTestResources", ":hermit-core:compileTestJava")
    ),
    TaskWithDependencyNames(
      ":hermit-core:classes",
      setOf(":hermit-core:compileJava", ":hermit-core:processResources")
    ),
    TaskWithDependencyNames(":hermit-core:compileKotlin", setOf()),
    TaskWithDependencyNames(":hermit-core:compileJava", setOf(":hermit-core:compileKotlin")),
    TaskWithDependencyNames(":hermit-core:lintKotlinMain", setOf()),
    TaskWithDependencyNames(":hermit-core:lintKotlinTest", setOf()),
    TaskWithDependencyNames(
      ":hermit-coroutines:apiBuild",
      setOf(":hermit-coroutines:compileKotlin", ":hermit-coroutines:compileJava")
    ),
    TaskWithDependencyNames(
      ":hermit-coroutines:compileTestJava",
      setOf(
        ":hermit-coroutines:classes",
        ":hermit-coroutines:compileKotlin",
        ":hermit-coroutines:compileJava",
        ":hermit-junit5:jar",
        ":hermit-core:jar",
        ":hermit-coroutines:compileTestKotlin"
      )
    ),
    TaskWithDependencyNames(
      ":hermit-coroutines:compileTestKotlin",
      setOf(
        ":hermit-coroutines:classes",
        ":hermit-coroutines:compileKotlin",
        ":hermit-coroutines:compileJava",
        ":hermit-junit5:jar",
        ":hermit-core:jar"
      )
    ),
    TaskWithDependencyNames(
      ":hermit-coroutines:testClasses",
      setOf(":hermit-coroutines:processTestResources", ":hermit-coroutines:compileTestJava")
    ),
    TaskWithDependencyNames(
      ":hermit-coroutines:classes",
      setOf(":hermit-coroutines:compileJava", ":hermit-coroutines:processResources")
    ),
    TaskWithDependencyNames(":hermit-coroutines:compileKotlin", setOf(":hermit-core:jar")),
    TaskWithDependencyNames(
      ":hermit-coroutines:compileJava",
      setOf(":hermit-core:jar", ":hermit-coroutines:compileKotlin")
    ),
    TaskWithDependencyNames(
      ":hermit-junit5:jar",
      setOf(
        ":hermit-junit5:classes",
        ":hermit-junit5:transformAtomicfuClasses",
        ":hermit-junit5:compileKotlin",
        ":hermit-junit5:compileJava"
      )
    ),
    TaskWithDependencyNames(
      ":hermit-core:jar",
      setOf(":hermit-core:classes", ":hermit-core:compileKotlin", ":hermit-core:compileJava")
    ),
    TaskWithDependencyNames(":hermit-coroutines:lintKotlinTest", setOf()),
    TaskWithDependencyNames(":hermit-coroutines:lintKotlinMain", setOf()),
    TaskWithDependencyNames(
      ":hermit-junit4:compileTestJava",
      setOf(
        ":hermit-junit4:classes",
        ":hermit-junit4:compileKotlin",
        ":hermit-junit4:compileJava",
        ":hermit-core:jar",
        ":hermit-junit4:compileTestKotlin"
      )
    ),
    TaskWithDependencyNames(
      ":hermit-junit4:compileTestKotlin",
      setOf(
        ":hermit-junit4:classes",
        ":hermit-junit4:compileKotlin",
        ":hermit-junit4:compileJava",
        ":hermit-core:jar"
      )
    ),
    TaskWithDependencyNames(
      ":hermit-junit4:testClasses",
      setOf(":hermit-junit4:processTestResources", ":hermit-junit4:compileTestJava")
    ),
    TaskWithDependencyNames(
      ":hermit-junit4:classes",
      setOf(":hermit-junit4:compileJava", ":hermit-junit4:processResources")
    ),
    TaskWithDependencyNames(":hermit-junit4:compileKotlin", setOf(":hermit-core:jar")),
    TaskWithDependencyNames(
      ":hermit-junit4:compileJava",
      setOf(":hermit-core:jar", ":hermit-junit4:compileKotlin")
    ),
    TaskWithDependencyNames(":hermit-junit4:lintKotlinTest", setOf()),
    TaskWithDependencyNames(":hermit-junit4:lintKotlinMain", setOf()),
    TaskWithDependencyNames(
      ":hermit-junit4:apiBuild",
      setOf(":hermit-junit4:compileKotlin", ":hermit-junit4:compileJava")
    ),
    TaskWithDependencyNames(
      ":hermit-junit5:compileTestJava",
      setOf(
        ":hermit-junit5:classes",
        ":hermit-junit5:transformAtomicfuClasses",
        ":hermit-junit5:compileKotlin",
        ":hermit-junit5:compileJava",
        ":hermit-core:jar",
        ":hermit-junit5:compileTestKotlin"
      )
    ),
    TaskWithDependencyNames(
      ":hermit-junit5:compileTestKotlin",
      setOf(
        ":hermit-junit5:classes",
        ":hermit-junit5:transformAtomicfuClasses",
        ":hermit-junit5:compileKotlin",
        ":hermit-junit5:compileJava",
        ":hermit-core:jar"
      )
    ),
    TaskWithDependencyNames(
      ":hermit-junit5:testClasses",
      setOf(":hermit-junit5:processTestResources", ":hermit-junit5:compileTestJava")
    ),
    TaskWithDependencyNames(
      ":hermit-junit5:transformTestAtomicfuClasses",
      setOf(
        ":hermit-junit5:classes",
        ":hermit-junit5:transformAtomicfuClasses",
        ":hermit-junit5:compileKotlin",
        ":hermit-junit5:compileJava",
        ":hermit-core:jar",
        ":hermit-junit5:testClasses"
      )
    ),
    TaskWithDependencyNames(
      ":hermit-junit5:classes",
      setOf(":hermit-junit5:compileJava", ":hermit-junit5:processResources")
    ),
    TaskWithDependencyNames(
      ":hermit-junit5:transformAtomicfuClasses",
      setOf(":hermit-core:jar", ":hermit-junit5:classes")
    ),
    TaskWithDependencyNames(":hermit-junit5:compileKotlin", setOf(":hermit-core:jar")),
    TaskWithDependencyNames(
      ":hermit-junit5:compileJava",
      setOf(":hermit-core:jar", ":hermit-junit5:compileKotlin")
    ),
    TaskWithDependencyNames(":hermit-junit5:lintKotlinTest", setOf()),
    TaskWithDependencyNames(":hermit-junit5:lintKotlinMain", setOf()),
    TaskWithDependencyNames(
      ":hermit-junit5:apiBuild",
      setOf(":hermit-junit5:compileKotlin", ":hermit-junit5:compileJava")
    ),
    TaskWithDependencyNames(":hermit-mockk:lintKotlinTest", setOf()),
    TaskWithDependencyNames(":hermit-mockk:lintKotlinMain", setOf()),
    TaskWithDependencyNames(
      ":hermit-mockk:compileTestJava",
      setOf(
        ":hermit-mockk:classes",
        ":hermit-mockk:compileKotlin",
        ":hermit-mockk:compileJava",
        ":hermit-core:jar",
        ":hermit-mockk:compileTestKotlin"
      )
    ),
    TaskWithDependencyNames(
      ":hermit-mockk:compileTestKotlin",
      setOf(
        ":hermit-mockk:classes",
        ":hermit-mockk:compileKotlin",
        ":hermit-mockk:compileJava",
        ":hermit-core:jar"
      )
    ),
    TaskWithDependencyNames(
      ":hermit-mockk:testClasses",
      setOf(":hermit-mockk:processTestResources", ":hermit-mockk:compileTestJava")
    ),
    TaskWithDependencyNames(
      ":hermit-mockk:classes",
      setOf(":hermit-mockk:compileJava", ":hermit-mockk:processResources")
    ),
    TaskWithDependencyNames(":hermit-mockk:compileKotlin", setOf(":hermit-core:jar")),
    TaskWithDependencyNames(
      ":hermit-mockk:compileJava",
      setOf(":hermit-core:jar", ":hermit-mockk:compileKotlin")
    ),
    TaskWithDependencyNames(
      ":hermit-mockk:apiBuild",
      setOf(":hermit-mockk:compileKotlin", ":hermit-mockk:compileJava")
    ),
    TaskWithDependencyNames(
      ":hermit-core:samples:compileTestJava",
      setOf(
        ":hermit-core:samples:classes",
        ":hermit-core:samples:compileKotlin",
        ":hermit-core:samples:compileJava",
        ":hermit-core:jar",
        ":hermit-core:samples:compileTestKotlin"
      )
    ),
    TaskWithDependencyNames(
      ":hermit-core:samples:compileTestKotlin",
      setOf(
        ":hermit-core:samples:classes",
        ":hermit-core:samples:compileKotlin",
        ":hermit-core:samples:compileJava",
        ":hermit-core:jar"
      )
    ),
    TaskWithDependencyNames(
      ":hermit-core:samples:testClasses",
      setOf(":hermit-core:samples:processTestResources", ":hermit-core:samples:compileTestJava")
    ),
    TaskWithDependencyNames(
      ":hermit-core:samples:classes",
      setOf(":hermit-core:samples:compileJava", ":hermit-core:samples:processResources")
    ),
    TaskWithDependencyNames(":hermit-core:samples:compileKotlin", setOf()),
    TaskWithDependencyNames(
      ":hermit-core:samples:compileJava",
      setOf(":hermit-core:samples:compileKotlin")
    ),
    TaskWithDependencyNames(":hermit-core:samples:lintKotlinTest", setOf()),
    TaskWithDependencyNames(":hermit-core:samples:lintKotlinMain", setOf()),
    TaskWithDependencyNames(":hermit-coroutines:samples:lintKotlinTest", setOf()),
    TaskWithDependencyNames(":hermit-coroutines:samples:lintKotlinMain", setOf()),
    TaskWithDependencyNames(
      ":hermit-coroutines:samples:compileTestKotlin",
      setOf(
        ":hermit-coroutines:samples:classes",
        ":hermit-coroutines:samples:compileKotlin",
        ":hermit-coroutines:samples:compileJava",
        ":hermit-coroutines:jar",
        ":hermit-junit5:jar",
        ":hermit-core:jar"
      )
    ),
    TaskWithDependencyNames(
      ":hermit-coroutines:samples:compileTestJava",
      setOf(
        ":hermit-coroutines:samples:classes",
        ":hermit-coroutines:samples:compileKotlin",
        ":hermit-coroutines:samples:compileJava",
        ":hermit-coroutines:jar",
        ":hermit-junit5:jar",
        ":hermit-core:jar",
        ":hermit-coroutines:samples:compileTestKotlin"
      )
    ),
    TaskWithDependencyNames(
      ":hermit-coroutines:samples:testClasses",
      setOf(
        ":hermit-coroutines:samples:processTestResources",
        ":hermit-coroutines:samples:compileTestJava"
      )
    ),
    TaskWithDependencyNames(
      ":hermit-coroutines:samples:classes",
      setOf(":hermit-coroutines:samples:compileJava", ":hermit-coroutines:samples:processResources")
    ),
    TaskWithDependencyNames(":hermit-coroutines:samples:compileKotlin", setOf()),
    TaskWithDependencyNames(
      ":hermit-coroutines:samples:compileJava",
      setOf(":hermit-coroutines:samples:compileKotlin")
    ),
    TaskWithDependencyNames(
      ":hermit-coroutines:jar",
      setOf(
        ":hermit-coroutines:classes",
        ":hermit-coroutines:compileKotlin",
        ":hermit-coroutines:compileJava"
      )
    ),
    TaskWithDependencyNames(
      ":hermit-junit4:samples:compileTestJava",
      setOf(
        ":hermit-junit4:samples:classes",
        ":hermit-junit4:samples:compileJava",
        ":hermit-junit4:samples:compileKotlin",
        ":hermit-junit4:jar",
        ":hermit-core:jar",
        ":hermit-junit4:samples:compileTestKotlin"
      )
    ),
    TaskWithDependencyNames(
      ":hermit-junit4:samples:compileTestKotlin",
      setOf(
        ":hermit-junit4:samples:classes",
        ":hermit-junit4:samples:compileJava",
        ":hermit-junit4:samples:compileKotlin",
        ":hermit-junit4:jar",
        ":hermit-core:jar"
      )
    ),
    TaskWithDependencyNames(
      ":hermit-junit4:samples:testClasses",
      setOf(":hermit-junit4:samples:processTestResources", ":hermit-junit4:samples:compileTestJava")
    ),
    TaskWithDependencyNames(
      ":hermit-junit4:samples:classes",
      setOf(":hermit-junit4:samples:compileJava", ":hermit-junit4:samples:processResources")
    ),
    TaskWithDependencyNames(
      ":hermit-junit4:samples:compileJava",
      setOf(":hermit-junit4:samples:compileKotlin")
    ),
    TaskWithDependencyNames(":hermit-junit4:samples:compileKotlin", setOf()),
    TaskWithDependencyNames(
      ":hermit-junit4:jar",
      setOf(":hermit-junit4:classes", ":hermit-junit4:compileKotlin", ":hermit-junit4:compileJava")
    ),
    TaskWithDependencyNames(":hermit-junit4:samples:lintKotlinTest", setOf()),
    TaskWithDependencyNames(":hermit-junit4:samples:lintKotlinMain", setOf()),
    TaskWithDependencyNames(":hermit-junit5:samples:lintKotlinMain", setOf()),
    TaskWithDependencyNames(":hermit-junit5:samples:lintKotlinTest", setOf()),
    TaskWithDependencyNames(
      ":hermit-junit5:samples:compileTestKotlin",
      setOf(
        ":hermit-junit5:samples:classes",
        ":hermit-junit5:samples:compileJava",
        ":hermit-junit5:samples:compileKotlin",
        ":hermit-junit5:jar",
        ":hermit-core:jar"
      )
    ),
    TaskWithDependencyNames(
      ":hermit-junit5:samples:compileTestJava",
      setOf(
        ":hermit-junit5:samples:classes",
        ":hermit-junit5:samples:compileJava",
        ":hermit-junit5:samples:compileKotlin",
        ":hermit-junit5:jar",
        ":hermit-core:jar",
        ":hermit-junit5:samples:compileTestKotlin"
      )
    ),
    TaskWithDependencyNames(
      ":hermit-junit5:samples:testClasses",
      setOf(":hermit-junit5:samples:processTestResources", ":hermit-junit5:samples:compileTestJava")
    ),
    TaskWithDependencyNames(
      ":hermit-junit5:samples:classes",
      setOf(":hermit-junit5:samples:compileJava", ":hermit-junit5:samples:processResources")
    ),
    TaskWithDependencyNames(
      ":hermit-junit5:samples:compileJava",
      setOf(":hermit-junit5:samples:compileKotlin")
    ),
    TaskWithDependencyNames(":hermit-junit5:samples:compileKotlin", setOf()),
    TaskWithDependencyNames(
      ":hermit-mockk:samples:compileTestKotlin",
      setOf(
        ":hermit-mockk:samples:classes",
        ":hermit-mockk:samples:compileKotlin",
        ":hermit-mockk:samples:compileJava",
        ":hermit-junit5:jar",
        ":hermit-mockk:jar",
        ":hermit-core:jar"
      )
    ),
    TaskWithDependencyNames(
      ":hermit-mockk:samples:compileTestJava",
      setOf(
        ":hermit-mockk:samples:classes",
        ":hermit-mockk:samples:compileKotlin",
        ":hermit-mockk:samples:compileJava",
        ":hermit-junit5:jar",
        ":hermit-mockk:jar",
        ":hermit-core:jar",
        ":hermit-mockk:samples:compileTestKotlin"
      )
    ),
    TaskWithDependencyNames(
      ":hermit-mockk:samples:testClasses",
      setOf(":hermit-mockk:samples:processTestResources", ":hermit-mockk:samples:compileTestJava")
    ),
    TaskWithDependencyNames(
      ":hermit-mockk:samples:classes",
      setOf(":hermit-mockk:samples:compileJava", ":hermit-mockk:samples:processResources")
    ),
    TaskWithDependencyNames(":hermit-mockk:samples:compileKotlin", setOf()),
    TaskWithDependencyNames(
      ":hermit-mockk:samples:compileJava",
      setOf(":hermit-mockk:samples:compileKotlin")
    ),
    TaskWithDependencyNames(
      ":hermit-mockk:jar",
      setOf(":hermit-mockk:classes", ":hermit-mockk:compileKotlin", ":hermit-mockk:compileJava")
    ),
    TaskWithDependencyNames(":hermit-mockk:samples:lintKotlinTest", setOf()),
    TaskWithDependencyNames(":hermit-mockk:samples:lintKotlinMain", setOf()),
    TaskWithDependencyNames(":processTestResources", setOf()),
    TaskWithDependencyNames(":processResources", setOf()),
    TaskWithDependencyNames(":hermit-core:processTestResources", setOf()),
    TaskWithDependencyNames(":hermit-core:processResources", setOf()),
    TaskWithDependencyNames(":hermit-coroutines:processTestResources", setOf()),
    TaskWithDependencyNames(":hermit-coroutines:processResources", setOf()),
    TaskWithDependencyNames(":hermit-junit4:processTestResources", setOf()),
    TaskWithDependencyNames(":hermit-junit4:processResources", setOf()),
    TaskWithDependencyNames(":hermit-junit5:processTestResources", setOf()),
    TaskWithDependencyNames(":hermit-junit5:processResources", setOf()),
    TaskWithDependencyNames(":hermit-mockk:processTestResources", setOf()),
    TaskWithDependencyNames(":hermit-mockk:processResources", setOf()),
    TaskWithDependencyNames(":hermit-core:samples:processTestResources", setOf()),
    TaskWithDependencyNames(":hermit-core:samples:processResources", setOf()),
    TaskWithDependencyNames(":hermit-coroutines:samples:processTestResources", setOf()),
    TaskWithDependencyNames(":hermit-coroutines:samples:processResources", setOf()),
    TaskWithDependencyNames(":hermit-junit4:samples:processTestResources", setOf()),
    TaskWithDependencyNames(":hermit-junit4:samples:processResources", setOf()),
    TaskWithDependencyNames(":hermit-junit5:samples:processTestResources", setOf()),
    TaskWithDependencyNames(":hermit-junit5:samples:processResources", setOf()),
    TaskWithDependencyNames(":hermit-mockk:samples:processTestResources", setOf()),
    TaskWithDependencyNames(":hermit-mockk:samples:processResources", setOf())
  )

  private val nodesWithNames by lazy {
    listOf(
      // Module A tasks
      TaskWithDependencyNames(
        ":a:check",
        mutableSetOf(":a:test", ":a:detekt", ":a:ktlintMainSourceSetCheck"),
        1.0
      ),
      TaskWithDependencyNames(":a:compileKotlin", mutableSetOf(":a:kspKotlin"), 1.0),
      TaskWithDependencyNames(
        ":a:compileTestKotlin",
        mutableSetOf(":a:kspTestKotlin", ":a:compileKotlin"),
        1.0
      ),
      TaskWithDependencyNames(":a:detekt", mutableSetOf(":a:detektMain", ":a:detektTest"), 1.0),
      TaskWithDependencyNames(":a:detektMain", mutableSetOf(":a:compileKotlin"), 1.0),
      TaskWithDependencyNames(":a:detektTest", mutableSetOf(":a:compileTestKotlin"), 1.0),
      TaskWithDependencyNames(":a:kspKotlin", mutableSetOf(), 1.0),
      TaskWithDependencyNames(
        ":a:kspTestKotlin",
        mutableSetOf(":a:kspKotlin", ":a:compileKotlin"),
        1.0
      ),
      TaskWithDependencyNames(":a:ktlintMainSourceSetCheck", mutableSetOf(), 1.0),
      TaskWithDependencyNames(":a:test", mutableSetOf(":a:compileTestKotlin"), 1.0),

      // Module B tasks
      TaskWithDependencyNames(
        ":b:check",
        mutableSetOf(":b:test", ":b:detekt", ":b:ktlintMainSourceSetCheck"),
        1.0
      ),
      TaskWithDependencyNames(
        ":b:compileKotlin",
        mutableSetOf(":b:kspKotlin", ":a:compileKotlin"),
        1.0
      ),
      TaskWithDependencyNames(
        ":b:compileTestKotlin",
        mutableSetOf(":b:kspTestKotlin", ":b:compileKotlin"),
        1.0
      ),
      TaskWithDependencyNames(":b:detekt", mutableSetOf(":b:detektMain", ":b:detektTest"), 1.0),
      TaskWithDependencyNames(":b:detektMain", mutableSetOf(":b:compileKotlin"), 1.0),
      TaskWithDependencyNames(":b:detektTest", mutableSetOf(":b:compileTestKotlin"), 1.0),
      TaskWithDependencyNames(":b:kspKotlin", mutableSetOf(), 1.0),
      TaskWithDependencyNames(
        ":b:kspTestKotlin",
        mutableSetOf(":b:kspKotlin", ":a:compileKotlin"),
        1.0
      ),
      TaskWithDependencyNames(":b:ktlintMainSourceSetCheck", mutableSetOf(), 1.0),
      TaskWithDependencyNames(":b:test", mutableSetOf(":b:compileTestKotlin"), 1.0),

      TaskWithDependencyNames(
        ":check",
        mutableSetOf(":a:check", ":b:check" /* ":c:check", ":d:check", ":e:check"*/),
        1.0
      )
    )
  }
  private val nodes by lazy { nodes1.removeRedundantDependencies().toNodes() }

  private val shards by lazy { nodes.toShards() }

  private val diagrams = File("build/tmp/diagrams")
  val shardGraph = diagrams / "shards.md"
  val taskWithDependencyNamesGraph = diagrams / "taskWithDependencyNames.md"
  val taskNodeGraph = diagrams / "taskNodes.md"
  val shartifyYaml = diagrams / "shartify.yml"

  fun File.mermaid(graph: String) {
    println("file://$absolutePath")
    writeText("```mermaid\n$graph```\n")
  }

  @Test
  fun `shard graph`() {

    shardGraph.mermaid(shards.mermaid())
    taskWithDependencyNamesGraph.mermaid(nodesWithNames.mermaid())
    taskNodeGraph.mermaid(nodes.mermaid())

    shartifyYaml.writeText(shards.workflow())
  }

  @Test
  fun `remove redundant dependencies`() {

    val n = listOf(
      TaskWithDependencyNames("a", mutableSetOf()),
      TaskWithDependencyNames("b", mutableSetOf("a")),
      TaskWithDependencyNames("c", mutableSetOf("a", "b")),
    )

    val expected = listOf(
      TaskWithDependencyNames("a", mutableSetOf()),
      TaskWithDependencyNames("b", mutableSetOf("a")),
      TaskWithDependencyNames("c", mutableSetOf("b")),
    ).joinToString("\n")

    n.removeRedundantDependencies().joinToString("\n") shouldBe expected
  }
}
