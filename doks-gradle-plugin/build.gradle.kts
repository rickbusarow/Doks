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

import com.rickbusarow.kgx.isRealRootProject

plugins {
  alias(libs.plugins.mahout.java.gradle.plugin)
  alias(libs.plugins.mahout.gradle.test)
  alias(libs.plugins.buildconfig)
  alias(libs.plugins.poko)
  alias(libs.plugins.kotlinx.serialization)
  alias(libs.plugins.gradle.plugin.publish)
  idea
}

val pluginId = "com.rickbusarow.doks"
val pluginArtifactId = "doks-gradle-plugin"
val moduleDescription = "the Doks Gradle plugin"

val pluginDeclaration: NamedDomainObjectProvider<PluginDeclaration> =
  gradlePlugin.plugins
    .register(pluginArtifactId) {
      id = pluginId
      displayName = "Doks"
      implementationClass = "com.rickbusarow.doks.DoksPlugin"
      version = mahoutProperties.versionName.get()
      description = moduleDescription
      this@register.tags.set(listOf("markdown", "documentation"))
    }

mahout {
  serialization()

  publishing {
    publishPlugin(pluginDeclaration)
  }
  gradleTests {}
}

val doksDeps = objects.setProperty<String>()
val doksParseDeps = objects.setProperty<String>()

buildConfig {

  sourceSets.named("main") {

    packageName(mahoutProperties.group.get())
    className("BuildConfig")

    useKotlinOutput {
      internalVisibility = true
    }

    buildConfigField("doksDeps", doksDeps)
    buildConfigField("doksParseDeps", doksParseDeps)
  }

  this@buildConfig.sourceSets.named(mahout.gradleTests.sourceSetName.get()) {

    packageName(mahoutProperties.group.get())
    className("GradleTestBuildConfig")

    useKotlinOutput {
      internalVisibility = true
    }

    buildConfigField("localBuildM2Dir", mahout.gradleTests.gradleTestM2Dir.asFile)

    buildConfigField("pluginId", pluginId)
    buildConfigField("doksVersion", mahoutProperties.versionName)
    buildConfigField("kotlinVersion", libs.versions.kotlin)
    buildConfigField("gradleVersion", gradle.gradleVersion)
  }
}

val mainConfig: Configuration = when {
  rootProject.isRealRootProject() -> configurations.compileOnly.get()
  else -> configurations.getByName("implementation")
}

fun Any.asExternalDependency(): ExternalDependency {
  return when (this) {
    is ExternalDependency -> this
    is org.gradle.api.internal.provider.TransformBackedProvider<*, *> -> this.get() as ExternalDependency
    is ProviderConvertible<*> -> this.asProvider().get() as ExternalDependency
    else -> error("unsupported dependency type -- ${this::class.java.canonicalName}")
  }
}

fun DependencyHandlerScope.worker(dependencyNotation: Any) {
  mainConfig(dependencyNotation)
  doksDeps.add(dependencyNotation.asExternalDependency().toString())
}

fun DependencyHandlerScope.workerSourceParsing(dependencyNotation: Any) {
  mainConfig(dependencyNotation)
  doksParseDeps.add(dependencyNotation.asExternalDependency().toString())
}

val gradleTestImplementation by configurations
val testImplementation by configurations

dependencies {

  compileOnly(gradleApi())

  for (testImpl in listOf(testImplementation, gradleTestImplementation)) {

    testImpl(libs.junit.jupiter)
    testImpl(libs.junit.jupiter.api)
    testImpl(libs.kotest.assertions.core.jvm)
    testImpl(libs.kotest.assertions.shared)
    testImpl(libs.kotest.common)
    testImpl(libs.kotest.property.jvm)
    testImpl(libs.rickBusarow.kase)
  }

  gradleTestImplementation(libs.kotlin.compiler)
  gradleTestImplementation(libs.rickBusarow.kase.gradle)
  gradleTestImplementation(libs.rickBusarow.kase.gradle.dsl)

  worker(libs.java.diff.utils)
  worker(libs.jetbrains.markdown)
  worker(libs.kotlinx.coroutines.core)
  worker(libs.kotlinx.serialization.core)
  worker(libs.kotlinx.serialization.core.jvm)
  worker(libs.kotlinx.serialization.json)
  worker(libs.kotlinx.serialization.json.jvm)

  workerSourceParsing(libs.kotlin.compiler)
  workerSourceParsing(libs.kotlinx.serialization.core)
  workerSourceParsing(libs.kotlinx.serialization.core.jvm)
  workerSourceParsing(libs.kotlinx.serialization.json)
  workerSourceParsing(libs.kotlinx.serialization.json.jvm)

  // implementation(libs.kotlinx.serialization.core)
  // implementation(libs.kotlinx.serialization.json)

  testImplementation(libs.java.diff.utils)
  testImplementation(libs.jetbrains.markdown)
  testImplementation(libs.kotlin.compiler)
  testImplementation(libs.kotlinx.coroutines.core)
  testImplementation(libs.kotlinx.serialization.core)
  testImplementation(libs.kotlinx.serialization.json)
}
