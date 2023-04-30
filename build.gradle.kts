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

import builds.GROUP
import builds.VERSION_NAME
import builds.mustRunAfter
import com.rickbusarow.doks.DoksTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
  id("root")
  alias(libs.plugins.moduleCheck)
  alias(libs.plugins.github.release)
  alias(libs.plugins.doks)
}

moduleCheck {
  deleteUnused = true
  checks.sortDependencies = true
}

doks {
  dokSet {
    docs("README.md", "CHANGELOG.md")

    sampleCodeSource("doks-gradle-plugin/src/integration/kotlin") {
      include("**/*.kt")
    }

    rule("kotlin-dsl-config-simple") {
      replacement = sourceCode(
        fqName = "com.rickbusarow.doks.ConfigTest.`kotlin dsl config simple`.config",
        bodyOnly = true,
        codeBlockLanguage = "kotlin",
        attributes = "title=\"build.gradle.kts\""
      )
    }

    rule("groovy-dsl-config-simple") {
      replacement = sourceCode(
        fqName = "com.rickbusarow.doks.ConfigTest.`groovy dsl config simple`.config",
        bodyOnly = true,
        codeBlockLanguage = "groovy",
        attributes = "title=\"build.gradle\""
      )
    }

    rule("kotlin-dsl-config-code") {
      replacement = sourceCode(
        fqName = "com.rickbusarow.doks.ConfigTest.`kotlin dsl config code`.config",
        bodyOnly = true,
        codeBlockLanguage = "kotlin",
        attributes = "title=\"build.gradle.kts\""
      )
    }

    rule("groovy-dsl-config-code") {
      replacement = sourceCode(
        fqName = "com.rickbusarow.doks.ConfigTest.`groovy dsl config code`.config",
        bodyOnly = true,
        codeBlockLanguage = "groovy",
        attributes = "title=\"build.gradle\""
      )
    }

    rule("dollar-raw-string") {
      regex = "\${'$'}".escapeRegex()
      replacement = "$".escapeReplacement()
    }
    rule("buildConfig-version") {
      regex = "\${BuildConfig.version}".escapeRegex()
      val version = libs.versions.rickBusarow.doks.get()
      replacement = version.escapeReplacement()
    }
    rule("plugin-with-version") {
      val version = libs.versions.rickBusarow.doks.get()
      regex = gradlePluginWithVersion(GROUP)
      replacement = "$1$2$3$4${version.escapeReplacement()}$6"
    }
    rule("doks-group") {
      regex = "com\\.(?:rickbusarow|square|squareup)\\.doks"
      replacement = GROUP
    }
  }
}

subprojects.map {
  it.tasks.withType(KotlinCompile::class.java)
    .mustRunAfter(tasks.withType(DoksTask::class.java))
}

// TODO move this to a convention plugin
githubRelease {

  token {
    property("GITHUB_PERSONAL_ACCESS_TOKEN") as? String
      ?: throw GradleException(
        "In order to release, you must provide a GitHub Personal Access Token " +
          "as a property named 'GITHUB_PERSONAL_ACCESS_TOKEN'."
      )
  }

  owner.set("rbusarow")

  tagName.set(VERSION_NAME)
  releaseName.set(VERSION_NAME)

  generateReleaseNotes.set(false)

  body.set(
    provider {

      if (VERSION_NAME.endsWith("-SNAPSHOT")) {
        throw GradleException(
          "do not create a GitHub release for a snapshot. (version is $VERSION_NAME)."
        )
      }

      val escapedVersion = Regex.escape(VERSION_NAME)

      // capture everything in between '## [<this version>]' and a new line which starts with '## '
      val versionSectionRegex = Regex(
        """(?:^|\n)## \[$escapedVersion]\s+.+\n([\s\S]*?)(?=\n+## |\[$escapedVersion])"""
      )

      versionSectionRegex
        .find(file("CHANGELOG.md").readText())
        ?.groupValues
        ?.getOrNull(1)
        ?.trim()
        ?.also { body ->
          if (body.isBlank()) {
            throw GradleException("The changelog for this version cannot be blank.")
          }
        }
        ?: throw GradleException(
          "could not find a matching change log for $versionSectionRegex"
        )
    }
  )

  overwrite.set(false)
  dryRun.set(false)
  draft.set(true)
}

val printTaskGraph by tasks.registering {

  group = "Help"
  description = "Prints the task graph starting from the root project's check task."

  // val graph = StringBuilder("graph TD\n")
  //
  // val rootTask = allprojects.mapNotNull { it.tasks.findByName("check") }
  // val taskQueue = ArrayDeque<Task>().apply { addAll(rootTask) }
  // val processedTasks = mutableSetOf<Task>()
  //
  // while (taskQueue.isNotEmpty()) {
  //   val task = taskQueue.removeFirst()
  //   if (!processedTasks.contains(task)) {
  //     processedTasks.add(task)
  //     graph.append("${task.path}[${task.path}]")
  //
  //     for (dependency in task.taskDependencies.getDependencies(task)) {
  //       val dependencyTask = dependency as Task
  //       if (
  //         dependencyTask.project != project &&
  //         !rootTask.any { it.dependsOn.contains(dependencyTask) }
  //       ) {
  //         graph.append("\n${task.path} --> ${dependencyTask.path}[${dependencyTask.path}]")
  //         if (dependencyTask != rootTask.first()) {
  //           rootTask.first().dependsOn(dependencyTask)
  //         }
  //       }
  //       taskQueue.addLast(dependencyTask)
  //     }
  //
  //     graph.append("\n")
  //   }
  // }
  //
  // // Generate code to initialize a list of nodes
  // val nodes = processedTasks.map { task ->
  //   val dependencyNames = task.taskDependencies.getDependencies(task)
  //     .mapNotNull { (it as? Task)?.path }
  //     .toSet()
  //   "TaskWithDependencyNames(${task.path.quote()}, setOf(${dependencyNames.joinToString { it.quote() }}))"
  // }
  //
  // doLast {
  //   println(graph)
  //   val foo = buildDir.resolve("foo.md")
  //   foo.writeText("```mermaid\n$graph\n```")
  //
  //   println("List of nodes:")
  //   println("val nodes = listOf(")
  //   println(nodes.joinToString(",\n"))
  //   println(")")
  // }

  doLast {
    println("#############################################")
    val tasksToCheck = mutableListOf<Task>()
    tasksToCheck.addAll(project.allprojects.mapNotNull { it.tasks.findByPath(":${it.name}:check") })

    val visited = mutableSetOf<Task>()
    val queue = tasksToCheck.toMutableList()

    while (queue.isNotEmpty()) {
      val task = queue.removeAt(0)
      if (task in visited) continue
      visited.add(task)

      println("############################################################## ${task.path}")
      println(task.dependsOnUnwrapped().joinToString("\n") {
        "${it.path.padEnd(70)}  ${it::class.java.canonicalName}"
      })
      println("##############################################################")

      val dependencies = task.dependsOnUnwrapped()
      queue.addAll(dependencies)
    }
  }
}

fun Any.tasks(project: Project): List<Task> {
  return when (val t = this) {
    is Task -> listOf(t)
    is String -> listOfNotNull(project.tasks.findByPath(t))
    is Provider<*> -> t.orNull?.tasks(project).orEmpty()
    is Iterable<*> -> t.flatMap { it?.tasks(project).orEmpty() }
    is Buildable -> t.buildDependencies.tasks(project)
    else -> {
      println("%%%%%%%%%%%%%%%%%%%%% bailing out on -- $t  --  ${t::class.java.canonicalName}")
      emptyList()
    }
  }
}

fun Task.dependsOnUnwrapped() = dependsOn.flatMap { it.tasks(project) }
fun String.quote() = "\"$this\""

data class TaskWithDependencyNames(
  val path: String,
  val dependencyNames: Set<String>,
  val weight: Double = 1.0
)
