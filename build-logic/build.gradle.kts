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

import com.rickbusarow.kgx.fromInt
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePlugin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
  dependencies {
    classpath(libs.kotlin.gradle.plugin)
    classpath(libs.vanniktech.publish)
    classpath(libs.rickBusarow.kgx)
  }
}

plugins {
  base
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.ktlint) apply false
}

val ktlintPluginId = libs.plugins.ktlint.get().pluginId

allprojects ap@{

  val kotlinApiVersion = project.property("KOTLIN_API").toString()

  val jdk = project.property("JDK_BUILD_LOGIC").toString()

  apply(plugin = ktlintPluginId)

  dependencies {
    "ktlint"(rootProject.libs.rickBusarow.ktrules)
  }

  plugins.withType(KotlinBasePlugin::class.java).configureEach {
    extensions.configure(KotlinJvmProjectExtension::class.java) {
      jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(jdk))
      }
      compilerOptions {
        jvmTarget = JvmTarget.fromInt(property("JVM_TARGET_BUILD_LOGIC").toString().toInt())
      }
    }
    java.targetCompatibility = JavaVersion.toVersion(property("JVM_TARGET_BUILD_LOGIC").toString())
  }

  tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {

      apiVersion = kotlinApiVersion

      freeCompilerArgs = freeCompilerArgs + listOf(
        "-opt-in=kotlin.RequiresOptIn"
      )
    }
  }
  tasks.withType<Test>().configureEach {
    useJUnitPlatform()
  }
}
