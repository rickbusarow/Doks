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
  private val nodes by lazy { nodesWithNames.removeRedundantDependencies().toNodes() }

  private val shards by lazy { nodes.toShards() }

  private val diagrams = File("build/tmp/diagrams")
  val shardGraph = diagrams / "shards.md"
  val taskWithDependencyNamesGraph = diagrams / "taskWithDependencyNames.md"
  val taskNodeGraph = diagrams / "taskNodes.md"

  fun File.mermaid(graph: String) {
    println("file://$absolutePath")
    writeText("```mermaid\n$graph```\n")
  }

  @Test
  fun `this is what I am thinking`() {

    val cleanedNodes = nodesWithNames.removeRedundantDependencies()

    taskWithDependencyNamesGraph.mermaid(cleanedNodes.mermaid())
  }

  @Test
  fun `shard graph`() {

    shardGraph.mermaid(shards.mermaid())
    taskWithDependencyNamesGraph.mermaid(nodesWithNames.mermaid())
    taskNodeGraph.mermaid(nodes.mermaid())

    println("####################################################")
    println(shards.workflow())
    println("####################################################")
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
