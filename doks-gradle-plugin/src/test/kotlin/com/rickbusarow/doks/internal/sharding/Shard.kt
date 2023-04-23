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

import com.rickbusarow.doks.internal.stdlib.indent
import com.rickbusarow.doks.internal.stdlib.trimLineEnds

data class TaskWithDependencyNames(
  val path: String,
  val dependencyNames: Set<String>,
  val weight: Double = 1.0
) {
  companion object {

    fun List<TaskWithDependencyNames>.removeRedundantDependencies(): List<TaskWithDependencyNames> {
      val tasksMap = associateBy { it.path }

      fun collectDependencies(
        taskPath: String,
        visited: MutableSet<String> = mutableSetOf()
      ): Set<String> {
        val task = tasksMap[taskPath] ?: return emptySet()
        if (taskPath in visited) return emptySet()
        visited.add(taskPath)

        val dependencies = task.dependencyNames.toMutableSet()
        for (dependencyName in task.dependencyNames) {
          dependencies.addAll(collectDependencies(dependencyName, visited))
        }

        return dependencies
      }

      return map { task ->
        val directDependencies = task.dependencyNames
        val indirectDependencies = directDependencies.flatMap { collectDependencies(it) }.toSet()

        TaskWithDependencyNames(
          task.path,
          directDependencies - indirectDependencies,
          task.weight
        )
      }
    }

    fun List<TaskWithDependencyNames>.mermaid(): String = buildString {

      val levelMap = buildMap {
        val queue = this@mermaid.toMutableList()

        var level = 0
        while (queue.isNotEmpty()) {

          queue
            // Find all remaining nodes whose dependencies are all already in the map. `all { }` returns
            // true if the collection is empty, so the first pass will return leaf nodes.
            .filter { node ->
              node.dependencyNames.all { dependencyName -> dependencyName in keys }
            }
            .forEach { node ->
              queue.remove(node)
              put(node.path, level)
            }
          level++
        }
      }

      // Generate the Mermaid graph
      appendLine("graph RL")

      // Iterate over the nodes and their dependencies
      this@mermaid.forEach { node ->
        node.dependencyNames.forEach { depPath ->

          val levelDiff = levelMap.getValue(node.path) - levelMap.getValue(depPath)

          val edge = "-".repeat(levelDiff) + "->"

          appendLine("${node.path} $edge $depPath")
        }
      }
    }
  }
}

data class TaskNode(
  val path: String,
  val weight: Double,
  val upstream: MutableSet<TaskNode>,
  private val downstreamLazy: Lazy<Set<TaskNode>>
) {
  val downstream: Set<TaskNode> by downstreamLazy

  val level: Int by lazy { upstream.maxOfOrNull { it.level }?.plus(1) ?: 0 }

  override fun toString(): String {
    return "TaskNode(path='$path', weight=$weight, upstream=${upstream.map { it.path }}, downstream=${downstream.map { it.path }}, level=$level)"
  }

  companion object {

    fun List<TaskWithDependencyNames>.toNodes(): List<TaskNode> {
      val taskNodes = map { task ->
        TaskNode(task.path, task.weight, mutableSetOf(), lazy { mutableSetOf<TaskNode>() })
      }.associateBy { it.path }

      for (taskWithDeps in this) {
        val taskNode = taskNodes[taskWithDeps.path]!!

        for (dependencyName in taskWithDeps.dependencyNames) {
          val dependencyNode = taskNodes[dependencyName]!!
          taskNode.upstream.add(dependencyNode)
          (dependencyNode.downstream as MutableSet).add(taskNode)
        }
      }

      return taskNodes.values.toList()
    }

    fun List<TaskNode>.mermaid(): String = buildString {

      appendLine("graph RL")

      this@mermaid.sortedBy { it.level }.forEach { node ->

        node.upstream.forEach { depNode ->

          val levelDiff = node.level - depNode.level

          val edge = "-".repeat(levelDiff - 1) + "-->"

          appendLine("${node.path} $edge ${depNode.path}")
        }
      }
    }
  }
}

data class Shard(
  val id: Int,
  val tasks: Set<TaskNode>,
  val dependencyShards: Set<Int>
) {

  val level: Int by lazy { tasks.maxOf { it.level } + 1 }

  override fun toString(): String {
    return "Shard(id=$id, tasks=${tasks.map { it.path }}, dependencyShards=$dependencyShards, level=$level)"
  }

  companion object {

    fun List<TaskNode>.toShards(): List<Shard> {

      val queue = this@toShards
        .sortedBy { it.path }
        .sortedBy { it.level }.toMutableList()
      val taskToShard = mutableMapOf<TaskNode, Shard>()

      return buildList shards@{

        while (queue.isNotEmpty()) {
          val head = queue.removeFirst()

          val streak = generateSequence(head) { headOrSingleUpstream ->

            headOrSingleUpstream.downstream
              .singleOrNull()
              ?.takeIf { it.upstream.size == 1 }
              ?.also { queue.remove(it) }
          }.toSet()

          streak.forEach { sn ->
            sn.upstream.forEach { snUpstream ->
              require(snUpstream !in queue) { "${sn.path} depends on ${snUpstream.path}" }
            }
          }

          val shardDeps = streak.flatMap { it.upstream }
            .filterNot { it in streak }
            .mapTo(mutableSetOf()) { taskToShard.getValue(it) }

          val newShard = Shard(
            id = this@shards.size,
            tasks = streak,
            dependencyShards = shardDeps.mapTo(mutableSetOf()) { it.id }
          )

          add(newShard)

          streak.forEach { taskToShard[it] = newShard }
        }
      }
    }

    fun List<Shard>.mermaid(): String = buildString {

      appendLine("graph RL")

      this@mermaid.forEach { shard ->

        appendLine(buildString {
          appendLine("subgraph shard_${shard.id}__${shard.level}")

          shard.tasks.forEach { taskNode ->

            appendLine("  ${taskNode.path}")
          }

          appendLine("end")
        }.prependIndent("  "))
      }

      this@mermaid.forEach { shard ->
        shard.dependencyShards.forEach { depShardId ->

          val depShard = this@mermaid.single { it.id == depShardId }
          //
          // val levelDiff = shard.level - depShard.level
          //
          // require(levelDiff >= 0) { "${shard.toStringPretty()}\n-----\n${depShard.toStringPretty()}" }
          //
          // val edge = "-".repeat(levelDiff) + "-->"

          val edge = "-->"

          appendLine("shard_${shard.id}__${shard.level} $edge shard_${depShardId}__${depShard.level}")
        }

        shard.tasks.sortedBy { it.level }.forEach { taskNode ->

          taskNode.upstream.forEach { depNode ->

            val levelDiff = taskNode.level - depNode.level

            val edge = "-".repeat(levelDiff - 1) + "-->"

            appendLine("${taskNode.path} $edge ${depNode.path}")
          }
        }
      }
    }

    fun List<Shard>.workflow(): String = buildString {

      val models = this@workflow

      appendLine("name: Shartify")
      appendLine()

      appendLine("on:")
      indent {
        appendLine("pull_request:")
        appendLine("workflow_dispatch:")
      }

      appendLine("env:")
      indent {
        append("macosGradleArgs: ")
        appendLine("\"-Dorg.gradle.jvmargs=-Xmx10g\"")
        append("ubuntuGradleArgs: ")
        appendLine("\"-Dorg.gradle.jvmargs=-Xmx5g\"")
        append("windowsGradleArgs: ")
        appendLine("\"-Dorg.gradle.jvmargs=-Xmx4g\"")
      }

      appendLine("concurrency:")
      indent {
        appendLine("group: ci-\${{ github.ref }}-\${{ github.head_ref }}")
        appendLine("cancel-in-progress: true")
      }

      appendLine("jobs:")

      models.forEach { shard ->

        indent {

          appendLine("shard_${shard.id}:")

          indent {
            appendLine("runs-on: ubuntu-latest")

            if (shard.dependencyShards.isNotEmpty()) {
              appendLine("needs:")
              indent {
                shard.dependencyShards.forEach { depShardId ->
                  appendLine("- shard_$depShardId")
                }
              }
            }

            appendLine()
            appendLine("steps:")

            indent {
              appendLine("- name: check out with token")
              appendLine("  uses: actions/checkout@v3")
            }

            indent {
              appendLine("- name: Set up JDK")
              indent {
                appendLine("uses: actions/setup-java@v3")
                appendLine("with:")

                indent {
                  appendLine("distribution: \"zulu\"")
                  appendLine("java-version: \"11\"")
                }
              }
            }

            indent {
              appendLine("- name: the tasks")
              indent {
                appendLine("uses: gradle/gradle-build-action@v2")
                appendLine("with:")
                indent {
                  appendLine("arguments: |")
                  shard.tasks.sortedBy { it.path }.forEach { task ->
                    appendLine("  ${task.path}")
                  }
                  appendLine("  \"\${{ env.ubuntuGradleArgs }}\"")
                  appendLine("cache-read-only: false")
                }
              }
            }

            // indent {
            //   appendLine("- name: Archive test results")
            //   appendLine("  uses: actions/upload-artifact@v3")
            //   appendLine("  if: failure()")
            //   appendLine("  with:")
            //   appendLine("    name: test-results-ubuntu")
            //   appendLine("    path: ./**/build/reports/tests/test")
            // }

            // indent {
            //   appendLine("- name: Unit test results")
            //   appendLine("  uses: mikepenz/action-junit-report@v3")
            //   appendLine("  if: failure()")
            //   appendLine("  with:")
            //   appendLine("    github_token: \${{ secrets.GITHUB_TOKEN }}")
            //   appendLine("    report_paths: \"**/build/**/TEST-*.xml\"")
            //   appendLine("    check_name: Unit Test Results - ubuntu")
            // }
          }
        }
      }

      indent {
        appendLine("shartify-all-green:")
        indent {
          appendLine("if: always()")
          appendLine("runs-on: ubuntu-latest")
          appendLine("needs:")
          models.forEach { shard ->
            appendLine("  - shard_${shard.id}")
          }
          appendLine()
          appendLine("steps:")
          appendLine("  - name: require that all other jobs have passed")
          appendLine("    uses: re-actors/alls-green@release/v1")
          appendLine("    with:")
          appendLine("      jobs: \${{ toJSON(needs) }}")
        }
      }
    }
      .trimLineEnds()
      .replace("\\n{3,}".toRegex(), "\n\n")
  }
}
