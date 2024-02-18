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

import com.rickbusarow.kase.files.HasWorkingDir
import com.rickbusarow.kase.files.JavaFileFileInjection
import com.rickbusarow.kase.files.LanguageInjection
import com.rickbusarow.kase.files.TestLocation
import com.rickbusarow.kase.gradle.DefaultGradleTestEnvironment
import com.rickbusarow.kase.gradle.DslLanguage
import com.rickbusarow.kase.gradle.GradleDependencyVersion
import com.rickbusarow.kase.gradle.GradleProjectBuilder
import com.rickbusarow.kase.gradle.GradleRootProjectBuilder
import com.rickbusarow.kase.gradle.GradleTestEnvironmentFactory
import com.rickbusarow.kase.gradle.dsl.BuildFileSpec
import com.rickbusarow.kase.gradle.rootProject
import java.io.File

internal class DoksGradleTestEnvironment(
  gradleVersion: GradleDependencyVersion,
  override val dslLanguage: DslLanguage,
  hasWorkingDir: HasWorkingDir,
  override val rootProject: GradleRootProjectBuilder
) : DefaultGradleTestEnvironment(
  gradleVersion = gradleVersion,
  dslLanguage = dslLanguage,
  hasWorkingDir = hasWorkingDir,
  rootProject = rootProject
),
  LanguageInjection<File> by LanguageInjection(JavaFileFileInjection()) {

  val GradleProjectBuilder.buildFileAsFile: File
    get() = path.resolve(dslLanguage.buildFileName)
  val GradleProjectBuilder.settingsFileAsFile: File
    get() = path.resolve(dslLanguage.settingsFileName)

  class Factory : GradleTestEnvironmentFactory<DoksGradleTestParams, DoksGradleTestEnvironment> {

    override val localM2Path: File
      get() = BuildConfig.localBuildM2Dir

    override fun buildFileDefault(versions: DoksGradleTestParams): BuildFileSpec =
      BuildFileSpec {
        plugins {
          kotlin("jvm", versions.kotlinVersion)
          id("com.rickbusarow.doks", version = BuildConfig.doksVersion)
        }
      }

    override fun create(
      params: DoksGradleTestParams,
      names: List<String>,
      location: TestLocation
    ): DoksGradleTestEnvironment {
      val hasWorkingDir = HasWorkingDir(names, location)

      return DoksGradleTestEnvironment(
        gradleVersion = params.gradle,
        dslLanguage = params.dslLanguage,
        hasWorkingDir = hasWorkingDir,
        rootProject = rootProject(
          path = hasWorkingDir.workingDir,
          dslLanguage = params.dslLanguage
        ) {
          buildFile(buildFileDefault(params))
          settingsFile(settingsFileDefault(params))
        }

      )
    }
  }
}
